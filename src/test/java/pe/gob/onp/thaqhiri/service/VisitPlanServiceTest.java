package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.auth.SaaTokenDetails;
import pe.gob.onp.thaqhiri.dto.VisitItemCompleteRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemResponse;
import pe.gob.onp.thaqhiri.dto.VisitPlanRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanResponse;
import pe.gob.onp.thaqhiri.entity.Destino;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.entity.VisitItem;
import pe.gob.onp.thaqhiri.entity.VisitPlan;
import pe.gob.onp.thaqhiri.model.VisitItemState;
import pe.gob.onp.thaqhiri.model.VisitPlanStatus;
import pe.gob.onp.thaqhiri.repository.VisitItemHistoryRepository;
import pe.gob.onp.thaqhiri.repository.VisitItemRepository;
import pe.gob.onp.thaqhiri.repository.VisitItemReassignHistoryRepository;
import pe.gob.onp.thaqhiri.repository.VisitPlanRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import pe.gob.onp.thaqhiri.repository.DestinoRepository;
import pe.gob.onp.thaqhiri.util.UConstante;

class VisitPlanServiceTest {

    @Mock
    private VisitPlanRepository planRepository;
    @Mock
    private VisitItemRepository itemRepository;
    @Mock
    private VisitItemHistoryRepository historyRepository;
    @Mock
    private VisitItemReassignHistoryRepository reassignHistoryRepository;
    @Mock
    private UserService userService;
    @Mock
    private DestinoService destinoService;
    @Mock
    private DestinoRepository destinoRepository;

    private VisitPlanService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Ajusta mocks para repositorio de reprogramacion][obj: VisitPlanServiceTest.setUp]
        service = new VisitPlanService(planRepository, itemRepository, historyRepository, reassignHistoryRepository, userService, destinoService, destinoRepository);
    }

    @Test
    void createPlan_savesPlanAndItems() {
        var req = new VisitPlanRequest(null, 2L, "Plan 1", LocalDate.now(), List.of(), "admin");
        var verifier = new User(); verifier.setId(2L); verifier.setNombre("Verifier");

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: createPlan usa getEntity por verifierId][obj: VisitPlanServiceTest.createPlan_savesPlanAndItems]
        when(userService.getEntity(2L)).thenReturn(verifier);
        when(planRepository.save(any(VisitPlan.class))).thenAnswer(i -> {
            VisitPlan p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        VisitPlanResponse resp = service.createPlan(req, "prueba", "prueba");

        assertNotNull(resp);
        assertEquals("Plan 1", resp.title());
        verify(planRepository).save(any(VisitPlan.class));
    }

    @Test
    void getPlanForVerifier_returnsAssignedPlan() {
        var verifier = new User(); verifier.setId(2L); verifier.setNombre("Verifier");
        when(userService.getEntityBySaaSubject("uid1")).thenReturn(verifier);

        var plan = new VisitPlan();
        plan.setId(10L);
        plan.setVerifier(verifier);
        plan.setStatus(VisitPlanStatus.PLANNED);
        
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Ajusta busqueda a metodo vigente en repositorio][obj: VisitPlanServiceTest.getPlanForVerifier_returnsAssignedPlan]
        when(planRepository.findFirstByVerifierIdAndPlannedForAndStRegiOrderByCreatedAtDesc(2L, LocalDate.now(), UConstante.ACTIVO_REGI))
                .thenReturn(Optional.of(plan));

        VisitPlanResponse resp = service.getPlanForVerifier(mockPrincipal("uid1"));

        assertEquals(10L, resp.id());
    }

    @Test
    void completeVisit_updatesStateAndHistory() {
        var verifier = new User(); verifier.setId(2L); verifier.setUsuario("verifier");
        when(userService.getEntityBySaaSubject("uid1")).thenReturn(verifier);

        var plan = new VisitPlan();
        plan.setVerifier(verifier);
        
	        var item = new VisitItem();
	        item.setId(100L);
	        item.setPlan(plan);
	        item.setState(VisitItemState.IN_VISIT);
	        var destino = new Destino();
	        destino.setId(1L);
	        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-06 00:00 UTC-5 (Lima)][desc: Completa datos mínimos de destino para el mapper (nombre/coords) en response de item][obj: VisitPlanServiceTest.completeVisit_updatesStateAndHistory]
	        destino.setNombre("Destino");
	        item.setDestino(destino);

	        when(itemRepository.findById(100L)).thenReturn(Optional.of(item));

        var req = new VisitItemCompleteRequest(true, false, null, "Info", null, null, null);
        VisitItemResponse resp = service.completeVisit(100L, req, mockPrincipal("uid1"), "prueba", "prueba");

        assertEquals(VisitItemState.DONE, resp.state());
        verify(historyRepository).save(any());
    }

    private SaaPrincipal mockPrincipal(String uid) {
        var details = new SaaTokenDetails("token", uid, "usuario", null,
                Instant.now().plusSeconds(300), List.of(), Map.of());
        return new SaaPrincipal(details);
    }
}
