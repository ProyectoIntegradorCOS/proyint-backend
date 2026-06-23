package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.auth.SaaTokenDetails;
import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeterRegistry meterRegistry;

    @MockBean
    private UserService userService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void contextLoads() {
        // Verifica que el contexto carga sin dependencias SAA
    }

    private void authenticate(String uid) {
        var details = new SaaTokenDetails("token", uid, "usuario", null,
                Instant.now().plusSeconds(300), List.of(), Map.of());
        var principal = new SaaPrincipal(details);
        var auth = new UsernamePasswordAuthenticationToken(principal, "token", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
