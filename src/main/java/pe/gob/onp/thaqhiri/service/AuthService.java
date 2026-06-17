package pe.gob.onp.thaqhiri.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.gob.onp.thaqhiri.auth.JwtService;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtService jwtService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AuthService(JwtService jwtService, UserService userService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    public String generarToken(JsonNode requestBody) throws Exception {
        String usuario = extractField(requestBody, "usuario");
        String clave   = extractField(requestBody, "clave");

        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("El campo 'usuario' es requerido");
        }
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("El campo 'clave' es requerido");
        }

        usuario = usuario.toUpperCase().trim();

        UserResponse user;
        try {
            user = userService.getByUsuario(usuario);
        } catch (ResourceNotFoundException e) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (!userService.verifyPassword(usuario, clave)) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario, user.nombre());

        return objectMapper.createObjectNode()
                .put("token", token)
                .put("idUsuaSist", user.id())
                .toString();
    }

    public String cerrarSesion(String token) {
        log.info("Cierre de sesión solicitado");
        return "{\"mensaje\":\"Sesión cerrada correctamente\"}";
    }

    private String extractField(JsonNode body, String field) {
        if (body == null || !body.has(field) || body.get(field).isNull()) return null;
        return body.get(field).asText();
    }
}
