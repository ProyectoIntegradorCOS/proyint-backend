package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.auth.SaaTokenDetails;
import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.dto.DailyDistanceResponse;
import pe.gob.onp.thaqhiri.dto.LocationHistoryResponse;
import pe.gob.onp.thaqhiri.dto.LocationResponse;
import pe.gob.onp.thaqhiri.service.LocationService;
import pe.gob.onp.thaqhiri.service.UbicacionesService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LocationController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private LocationService locationService;
    @MockBean
    private UbicacionesService ubicacionesService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void history_returnsPayload() throws Exception {
        authenticate("abc");
        var now = OffsetDateTime.now();
        var resp = new LocationHistoryResponse(
                "abc",
                now.minusHours(1),
                now,
                List.of(new LocationResponse(1L, -12.05, -77.05, now, null, null, null, null, null, null)),
                1.23
        );
        Mockito.when(locationService.getHistory(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(resp);

        mockMvc.perform(get("/api/locations/history")
                        .param("saaSubject", "abc")
                        .param("start", now.minusHours(1).toString())
                        .param("end", now.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saaSubject").value("abc"))
                .andExpect(jsonPath("$.points[0].latitude").value(-12.05));
    }

    @Test
    void distance_returnsPayload() throws Exception {
        authenticate("abc");
        var today = LocalDate.now();
        var resp = new DailyDistanceResponse("abc", today, 2.5);
        Mockito.when(locationService.getDailyDistance(Mockito.anyString(), Mockito.any()))
                .thenReturn(resp);

        mockMvc.perform(get("/api/locations/distance")
                        .param("saaSubject", "abc")
                        .param("date", today.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(2.5));
    }

    @Test
    void createBatch_returnsCount() throws Exception {
        authenticate("abc");
        Mockito.when(locationService.createBatch(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(5);

        String json = """
            {
              "locations": [
                {
                  "saaSubject": "abc",
                  "latitude": -12.0,
                  "longitude": -77.0,
                  "timestamp": "2023-10-10T10:00:00Z",
                  "accuracy": 10.0,
                  "altitude": 100.0,
                  "speed": 0.0,
                  "heading": 0.0,
                  "batteryLevel": 100.0,
                  "activityType": "still"
                }
              ]
            }
            """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/locations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(5));
    }

    private void authenticate(String uid) {
        var details = new SaaTokenDetails("token", uid, "usuario", null,
                Instant.now().plusSeconds(300), List.of(), Map.of());
        var principal = new SaaPrincipal(details);
        var auth = new UsernamePasswordAuthenticationToken(principal, "token", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
