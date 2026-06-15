package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.auth.SaaAuthenticationFilter;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.auth.SaaTokenDetails;
import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.dto.VisitItemCompleteRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemResponse;
import pe.gob.onp.thaqhiri.dto.VisitPlanResponse;
import pe.gob.onp.thaqhiri.model.VisitPlanStatus;
import pe.gob.onp.thaqhiri.model.VisitItemState;
import pe.gob.onp.thaqhiri.service.VisitPlanImportService;
import pe.gob.onp.thaqhiri.service.VisitPlanService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = VisitPlanController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class,
                SaaAuthenticationFilter.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:07 UTC-5 (Lima)][desc: Agrega mock de import masivo para endpoints de plantilla/import de planes][obj: VisitPlanControllerTest]
class VisitPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VisitPlanService visitPlanService;

    @MockBean
    private VisitPlanImportService visitPlanImportService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPlanForVerifier_returnsPlan() throws Exception {
        authenticate("uid1");
        var plan = new VisitPlanResponse(
                1L,
                "Plan Title",
                LocalDate.now(),
                VisitPlanStatus.PLANNED,
                1L,
                "Verifier",
                "Equipo 1",
                1L,
                1L,
                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Ajusta test al nuevo VisitPlanResponse con inicio de plan][obj: VisitPlanControllerTest.getPlanForVerifier_returnsPlan]
                null,
                null,
                null,
                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Ajusta test al nuevo VisitPlanResponse con fin de plan][obj: VisitPlanControllerTest.getPlanForVerifier_returnsPlan]
                null,
                null,
                null,
                List.of()
        );

        Mockito.when(visitPlanService.getPlanForVerifier(Mockito.any(SaaPrincipal.class)))
                .thenReturn(plan);

        mockMvc.perform(get("/api/visit-plans/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void completeVisit_returnsUpdatedItem() throws Exception {
        authenticate("uid1");
	        var item = new VisitItemResponse(
	                10L,
	                "Target",
	                null,
	                1,
	                "NORMAL",
	                null,
	                VisitItemState.DONE,
	                null,
	                null,
	                "Info",
	                null,
	                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-06 00:00 UTC-5 (Lima)][desc: Ajusta test al nuevo VisitItemResponse con lat/lng del destino][obj: VisitPlanControllerTest.completeVisit_returnsUpdatedItem]
	                null,   // destinoId
	                null,   // destinoNombre
	                null,   // latitude
	                null    // longitude
	        );

        Mockito.when(visitPlanService.completeVisit(Mockito.eq(10L), Mockito.any(), Mockito.any(), "prueba", "prueba"))
                .thenReturn(item);

        String json = """
            {
              "complex": true,
              "foundProblem": false,
              "otherInfo": "Info"
            }
            """;

        mockMvc.perform(post("/api/visit-plans/items/10/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    private void authenticate(String uid) {
        var details = new SaaTokenDetails("token", uid, "usuario", null,
                Instant.now().plusSeconds(300), List.of(), Map.of());
        var principal = new SaaPrincipal(details);
        var auth = new UsernamePasswordAuthenticationToken(principal, "token", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
