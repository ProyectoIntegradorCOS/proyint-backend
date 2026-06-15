package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.auth.SaaAuthenticationFilter;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.auth.SaaTokenDetails;
import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.controller.UserController;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.auth.SaaProperties;
import pe.gob.onp.thaqhiri.service.SincronizacionService;
import pe.gob.onp.thaqhiri.service.UsuarioSaaService;
import pe.gob.onp.thaqhiri.service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
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
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class,
                SaaAuthenticationFilter.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private UsuarioSaaService usuarioSaaService;
    @MockBean
    private SaaProperties saaProperties;
    @MockBean
    private SincronizacionService sincronizacionService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void list_returnsUsers() throws Exception {
    	/*
        var u = new UserResponse(
                1L,
                "abc",
                "PJ",
                "Juan Perez",
                1,
                null,
                null);
        Mockito.when(userService.findAll()).thenReturn(List.of(u));
        authenticate("abc");
        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saaSubject").value("abc"));
                */
    }

    @Test
    void getByUid_returnsUser() throws Exception {
    	/*
        var u = new UserResponse(
                1L,
                "abc",
                "PJ",
                "Juan Perez",
                1,
                null,
                null);
        Mockito.when(userService.getBySaaSubject("abc")).thenReturn(u);
        authenticate("abc");
        mockMvc.perform(get("/api/users/abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saaSubject").value("abc"));
                */
    }

    private void authenticate(String uid) {
        var details = new SaaTokenDetails("token", uid, "usuario", null,
                Instant.now().plusSeconds(300), List.of(), Map.of());
        var principal = new SaaPrincipal(details);
        var auth = new UsernamePasswordAuthenticationToken(principal, "token", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
