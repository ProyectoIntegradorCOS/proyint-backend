package pe.gob.onp.thaqhiri.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pe.gob.onp.thaqhiri.auth.JwtService;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock private JwtService jwtService;
    @Mock private UserService userService;

    private AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(jwtService, userService, objectMapper);
    }

    @Test
    void generarToken_validCredentials_returnsJson() throws Exception {
        UserResponse user = new UserResponse(1L, null, "ADMIN", "Administrador", 1, null, null, null, null, null);
        when(userService.getByUsuario("ADMIN")).thenReturn(user);
        when(userService.verifyPassword("ADMIN", "Cambiar123!")).thenReturn(true);
        when(jwtService.generateToken("ADMIN", "Administrador")).thenReturn("jwt.token.here");

        String json = "{\"usuario\":\"ADMIN\",\"clave\":\"Cambiar123!\"}";
        String result = authService.generarToken(objectMapper.readTree(json));

        var parsed = objectMapper.readTree(result);
        assertEquals("jwt.token.here", parsed.get("token").asText());
        assertEquals(1, parsed.get("idUsuaSist").asInt());
    }

    @Test
    void generarToken_wrongPassword_throwsIllegalArgument() {
        UserResponse user = new UserResponse(1L, null, "ADMIN", "Administrador", 1, null, null, null, null, null);
        when(userService.getByUsuario("ADMIN")).thenReturn(user);
        when(userService.verifyPassword("ADMIN", "wrong")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                authService.generarToken(objectMapper.readTree("{\"usuario\":\"ADMIN\",\"clave\":\"wrong\"}")));
    }

    @Test
    void generarToken_unknownUser_throwsIllegalArgument() {
        when(userService.getByUsuario(anyString()))
                .thenThrow(new ResourceNotFoundException("no existe"));

        assertThrows(IllegalArgumentException.class, () ->
                authService.generarToken(objectMapper.readTree("{\"usuario\":\"NOEXISTE\",\"clave\":\"x\"}")));
    }

    @Test
    void generarToken_missingUsuario_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.generarToken(objectMapper.readTree("{\"clave\":\"x\"}")));
    }

    @Test
    void cerrarSesion_returnsSuccessMessage() throws Exception {
        String result = authService.cerrarSesion("any.token");
        assertTrue(result.contains("cerrada"));
    }
}
