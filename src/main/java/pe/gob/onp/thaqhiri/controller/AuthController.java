package pe.gob.onp.thaqhiri.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.onp.thaqhiri.service.AuthService;

@Tag(name = "Auth", description = "Autenticacion y gestion de sesiones")
@RestController
@RequestMapping({"/api/auth", "/api/auths"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final MeterRegistry meterRegistry;

    public AuthController(AuthService authService, MeterRegistry meterRegistry) {
        this.authService = authService;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica usuario/clave contra la BD local y devuelve un JWT")
    @PostMapping("/token")
    public ResponseEntity<?> generarToken(@RequestBody JsonNode requestBody) {
        try {
            log.debug("generarToken solicitado");
            String respuestaJson = authService.generarToken(requestBody);
            meterRegistry.counter("thaqhiri_backend_login_total", Tags.of("status", "success")).increment();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(respuestaJson);
        } catch (IllegalArgumentException e) {
            meterRegistry.counter("thaqhiri_backend_login_total", Tags.of("status", "error")).increment();
            return ResponseEntity.status(401)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Error generando token", e);
            meterRegistry.counter("thaqhiri_backend_login_total", Tags.of("status", "error")).increment();
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @Operation(summary = "Cerrar sesión", description = "Cierre de sesión (el cliente debe descartar el token)")
    @PostMapping("/logout")
    public ResponseEntity<?> cerrarSesion(@RequestHeader("Authorization") String authHeader) {
        try {
            log.debug("cerrarSesion solicitado");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body("{\"error\":\"Token no proporcionado o formato inválido\"}");
            }
            String token = authHeader.substring(7);
            String response = authService.cerrarSesion(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cerrando sesión", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error interno del servidor\"}");
        }
    }
}
