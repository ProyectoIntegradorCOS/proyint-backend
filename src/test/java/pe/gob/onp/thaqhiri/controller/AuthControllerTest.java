package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.service.AuthService;

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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private AuthService authService;

    @Test
    void login_returnsToken() throws Exception {
        String jsonResponse = "{\"token\":\"token123\",\"idUsuaSist\":1}";
        Mockito.when(authService.generarToken(Mockito.any())).thenReturn(jsonResponse);

        String json = """
            {
              "usuario": "ADMIN",
              "clave": "Cambiar123!"
            }
            """;

        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }

    @Test
    void logout_returnsSuccess() throws Exception {
        Mockito.when(authService.cerrarSesion(Mockito.anyString()))
                .thenReturn("{\"mensaje\":\"Sesión cerrada correctamente\"}");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer oldToken"))
                .andExpect(status().isOk());
    }
}
