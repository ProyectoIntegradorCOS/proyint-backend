package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.dto.LocationCreateRequest;
import pe.gob.onp.thaqhiri.entity.Location;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.repository.LocationRepository;
import pe.gob.onp.thaqhiri.service.LocationService;
import pe.gob.onp.thaqhiri.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationServiceTest {

    private LocationRepository repo;
    private UserService userService;
    private LocationService service;

    @BeforeEach
    void setup() {
        repo = mock(LocationRepository.class);
        userService = mock(UserService.class);
        service = new LocationService(repo, userService);
    }

    @Test
    void create_savesRecord() {
        var user = new User();
        user.setSaaSub("abc");
        user.setUsuario("PJ");
        user.setNombre("Juan Perez");
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Ajusta estado a ST_REGI string][obj: LocationServiceTest.create_savesRecord]
        user.setEstado(1);
        when(userService.getEntityBySaaSubject("abc")).thenReturn(user);

        Location saved = new Location();
        saved.setId(1L);
        saved.setUser(user);
        saved.setRecordedAt(OffsetDateTime.now());
        when(repo.save(any(Location.class))).thenReturn(saved);

        var req = new LocationCreateRequest(
                "abc", -12.0, -77.0, OffsetDateTime.now(), null, null, null, null, null, null
        );
        
        String usuario = "prueba";
        String terminal = "prueba";
        
        var resp = service.create(req, usuario, terminal);

        assertEquals(1L, resp.id());
        verify(repo, times(1)).save(any(Location.class));
    }

    @Test
    void history_usesRepositoryCalls() {
        var user = new User();
        user.setSaaSub("abc");
        user.setUsuario("PJ");
        user.setNombre("Juan Perez");
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Ajusta estado a ST_REGI string][obj: LocationServiceTest.history_usesRepositoryCalls]
        user.setEstado(1);
        user.setId(10L);
        when(userService.getEntityBySaaSubject("abc")).thenReturn(user);

        //when(repo.findByUserAndRecordedAtBetweenOrderByRecordedAtAsc(any(), any(), any()))
        //        .thenReturn(List.of());
        //when(repo.calculateDistanceMeters(any(), any(), any())).thenReturn(1500.0);

        var now = OffsetDateTime.now();
        var res = service.getHistory("abc", now.minusHours(1), now);
        assertEquals(1.5, res.totalDistanceKm(), 1e-6);
    }

    @Test
    void createBatch_savesMultipleRecords() {
        var user = new User();
        user.setSaaSub("abc");
        user.setUsuario("PJ");
        when(userService.getEntityBySaaSubject("abc")).thenReturn(user);

        var req1 = new LocationCreateRequest("abc", -12.0, -77.0, OffsetDateTime.now(), null, null, null, null, null, null);
        var req2 = new LocationCreateRequest("abc", -12.1, -77.1, OffsetDateTime.now(), null, null, null, null, null, null);
        
        var batch = new pe.gob.onp.thaqhiri.dto.LocationBatchRequest(List.of(req1, req2));

        when(repo.saveAll(anyList())).thenAnswer(inv -> {
            List<?> list = inv.getArgument(0);
            return list; 
        });

        String usuario = "prueba";
        String terminal = "prueba";
        int count = service.createBatch(batch, usuario, terminal);

        assertEquals(2, count);
        verify(userService, times(1)).getEntityBySaaSubject("abc"); // Should be called once for grouped user
        verify(repo, times(1)).saveAll(anyList());
    }

    @Test
    void createBatch_handlesEmptyList() {
        var batch = new pe.gob.onp.thaqhiri.dto.LocationBatchRequest(List.of());
        
        String usuario = "prueba";
        String terminal = "prueba";        
        
        int count = service.createBatch(batch, usuario, terminal);
        assertEquals(0, count);
        verifyNoInteractions(repo);
    }

    @Test
    void createBatch_handlesMultipleUsers() {
        var user1 = new User(); user1.setSaaSub("u1");
        var user2 = new User(); user2.setSaaSub("u2");
        when(userService.getEntityBySaaSubject("u1")).thenReturn(user1);
        when(userService.getEntityBySaaSubject("u2")).thenReturn(user2);

        var req1 = new LocationCreateRequest("u1", -12.0, -77.0, OffsetDateTime.now(), null, null, null, null, null, null);
        var req2 = new LocationCreateRequest("u2", -12.1, -77.1, OffsetDateTime.now(), null, null, null, null, null, null);

        var batch = new pe.gob.onp.thaqhiri.dto.LocationBatchRequest(List.of(req1, req2));
        
        when(repo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        String usuario = "prueba";
        String terminal = "prueba";
        
        int count = service.createBatch(batch, usuario, terminal);

        assertEquals(2, count);
        verify(userService, times(1)).getEntityBySaaSubject("u1");
        verify(userService, times(1)).getEntityBySaaSubject("u2");
        verify(repo, times(2)).saveAll(anyList()); // Called once per group
    }

    @Test
    void createBatch_throwsIfUserNotFound() {
        when(userService.getEntityBySaaSubject("unknown")).thenReturn(null);

        var req = new LocationCreateRequest("unknown", -12.0, -77.0, OffsetDateTime.now(), null, null, null, null, null, null);
        var batch = new pe.gob.onp.thaqhiri.dto.LocationBatchRequest(List.of(req));

        String usuario = "prueba";
        String terminal = "prueba";
        
        assertThrows(RuntimeException.class, () -> service.createBatch(batch, usuario, terminal));
    }

}
