package pe.gob.onp.thaqhiri.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pe.gob.onp.thaqhiri.auth.SaaProperties;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.entity.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SaaProperties saaProperties;

    @Mock
    private UserService userService;

    private AuthService authService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        //authService = new AuthService(restTemplateBuilder, objectMapper, saaProperties, userService);
    }

    @Test
    void generarToken_returnsJson() throws Exception {
        when(saaProperties.getGenerateUrl()).thenReturn("http://api/token");
        
        // Mock external API response
        // Payload: {"Usuario":"USER"} encoded in base64
        String payload = "{\"Usuario\":\"USER\"}";
        String base64Payload = java.util.Base64.getUrlEncoder().encodeToString(payload.getBytes());
        String token = "header." + base64Payload + ".signature";
        
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(token));

        // Mock UserService
        UserResponse user = new UserResponse(1L, null, null, null, null, null, null, null, null, null);        
        when(userService.getByUsuario("USER")).thenReturn(user);

        // Execute
        String json = "{\"semilla\":\"123\"}";
        String result = authService.generarToken(objectMapper.readTree(json));

        // Verify
        JsonNode parsed = objectMapper.readTree(result);
        assertEquals(token, parsed.get("token").asText());
        assertEquals(1, parsed.get("idUsuaSist").asInt());
    }

    @Test
    void cerrarSesion_callsApi() throws Exception {
        when(saaProperties.getCloseUrl()).thenReturn("http://api/logout");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Logged out"));

        String result = authService.cerrarSesion("token123");

        assertEquals("Logged out", result);
    }
}
