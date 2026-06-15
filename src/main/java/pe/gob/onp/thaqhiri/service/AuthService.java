package pe.gob.onp.thaqhiri.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.gob.onp.thaqhiri.auth.SaaProperties;
import pe.gob.onp.thaqhiri.auth.SaaTokenService;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SaaProperties saaProperties;
    private final UserService userService;
    private final SaaTokenService tokenService;

    public AuthService(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper, SaaProperties saaProperties, UserService userService, SaaTokenService tokenService) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
        this.saaProperties = saaProperties;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public String generarToken(JsonNode requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String bodyJson = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-09 11:10 UTC-5 (Lima)][desc: Enruta a SAA según payload (semilla vs usuario/clave)][obj: AuthService.generarToken route]
        String targetUrl = resolveGenerateUrl(requestBody);
        ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, entity, String.class);
        String token = response.getBody();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("SAA no devolvió token");
        }

        // Decodificar payload para obtener usuario
        String[] partes = token.split("\\.");
        if (partes.length < 2) {
            throw new IllegalArgumentException("Token inválido");
        }

        String base64Payload = partes[1];
        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(base64Payload));
        JsonNode payload = objectMapper.readTree(payloadJson);

        String loginUsuarioSaa = payload.has("Usuario") ? payload.get("Usuario").asText() : null;
        if (loginUsuarioSaa != null) {
            loginUsuarioSaa = loginUsuarioSaa.toUpperCase();
        }

        Long idUsuarioSistema = 0L;
        try {
            UserResponse userSistema = this.userService.getByUsuario(loginUsuarioSaa);
            if (userSistema != null) {
                idUsuarioSistema = userSistema.id();
            }
        } catch (ResourceNotFoundException ex) {
            // En algunos despliegues el usuario aún no está sincronizado en la BD local.
            // Se devuelve idUsuaSist=0 y el frontend puede disparar el flujo de sincronización.
        	log.warn("El usuario '" + loginUsuarioSaa + " no se encuentra activo en la base de datos de Thaqhiri.");
        	//throw(ex);        	
        }

        return objectMapper.createObjectNode()
                .put("token", token)
                .put("idUsuaSist", idUsuarioSistema)
                .toString();
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-09 11:10 UTC-5 (Lima)][desc: Selecciona endpoint SAA según estructura del request][obj: AuthService.resolveGenerateUrl]
    private String resolveGenerateUrl(JsonNode requestBody) {
        if (requestBody != null && requestBody.has("usuario")) {
            final String url = this.saaProperties.getUsuarioGenerateUrl();
            if (url != null && !url.isBlank()) return url;
        }
        return this.saaProperties.getGenerateUrl();
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Evicta el token del cache Caffeine antes de llamar a SAA para garantizar que no sea aceptado aunque el TTL residual no haya vencido][obj: AuthService.cerrarSesion]
    public String cerrarSesion(String token) throws Exception {
        tokenService.evict(token);
        String url = this.saaProperties.getCloseUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String bodyJson = "{\"token\":\"" + token + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();
    }

    public String renovarToken(String token) throws Exception {
        String url = this.saaProperties.getRenewUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String bodyJson = "{\"token\":\"" + token + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        return response.getBody();
    }
}
