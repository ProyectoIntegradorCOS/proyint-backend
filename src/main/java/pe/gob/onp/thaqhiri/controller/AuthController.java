package pe.gob.onp.thaqhiri.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import pe.gob.onp.thaqhiri.service.AuthService;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: AuthController swagger]
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
    
    @Operation(summary = "Generar token", description = "Genera un token SAA segun los datos del request")
    @PostMapping("/token")
    public ResponseEntity<?> generarToken(@RequestBody JsonNode requestBody) {
        try {
            log.debug("generarToken solicitado");
            String respuestaJson = authService.generarToken(requestBody);
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Registra métrica de inicio de sesión exitoso][obj: AuthController.generarToken]
            meterRegistry.counter("thaqhiri_backend_login_total", Tags.of("status", "success")).increment();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(respuestaJson);

        } catch (HttpClientErrorException e) {
            meterRegistry.counter("thaqhiri_backend_login_total", Tags.of("status", "error")).increment();
            return ResponseEntity.status(e.getStatusCode())
                                 .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error generando token", e);
            meterRegistry.counter("thaqhiri_backend_login_total", Tags.of("status", "error")).increment();
            return ResponseEntity.internalServerError()
                                 .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    
    /**
     * 🔐 Endpoint para cerrar sesión.
     * Recibe el token en la cabecera "Authorization: Bearer <token>"
     * y lo envía al backend remoto para invalidarlo.
     */
    @Operation(summary = "Cerrar sesion", description = "Invalida el token SAA del usuario autenticado")
    @PostMapping("/logout")
    public ResponseEntity<?> cerrarSesion(@RequestHeader("Authorization") String authHeader) {
        try {
            log.debug("cerrarSesion solicitado");

            // 1️⃣ Validar encabezado
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body("{\"error\":\"Token no proporcionado o formato inválido\"}");
            }

            // 2️⃣ Extraer el token sin el prefijo "Bearer "
            String token = authHeader.substring(7);
            String response = authService.cerrarSesion(token);
            return ResponseEntity.ok(response);

        } catch (HttpClientErrorException e) {
            log.warn("Error HTTP remoto en cierre de sesión: {}", e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error cerrando sesión", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @Operation(summary = "Renovar token", description = "Renueva el token SAA del usuario autenticado")
    @PostMapping("/renew")
    public ResponseEntity<?> renovarToken(@RequestHeader("Authorization") String authHeader) {
        try {
            log.debug("renovarToken solicitado");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body("{\"error\":\"Token no proporcionado o formato inválido\"}");
            }

            String token = authHeader.substring(7);
            String response = authService.renovarToken(token);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error renovando token", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error interno del servidor\"}");
        }
    }
}
