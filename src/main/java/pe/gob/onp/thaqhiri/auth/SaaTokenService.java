package pe.gob.onp.thaqhiri.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class SaaTokenService {

    private static final Logger log = LoggerFactory.getLogger(SaaTokenService.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final RestTemplate restTemplate;
    private final SaaProperties properties;
    private final ObjectMapper objectMapper;
    private final Cache<String, CachedToken> cache;

    public SaaTokenService(
            SaaProperties properties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(properties.getTimeout())
                .setReadTimeout(properties.getTimeout())
                .build();
        this.cache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(properties.getCache().getMinTtl())
                .build();
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Evicta un token específico del cache Caffeine al hacer logout, evitando que un token invalidado por SAA siga siendo aceptado durante el TTL residual][obj: SaaTokenService.evict]
    public void evict(String token) {
        if (token == null || token.isBlank()) return;
        cache.invalidate(token.trim());
        log.info("Token evictado del cache (logout)");
    }

    public SaaTokenDetails validate(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Token ausente en validate()");
            throw new InvalidTokenException("Token ausente");
        }
        final String normalized = token.trim();
        log.debug("Token recibido");
        final CachedToken cached = cache.getIfPresent(normalized);
        if (cached != null && cached.isStillValid(properties.getCache().getMinTtl())) {
            log.debug("Token en cache y todavía válido — sub={} usuario={} expira={}",
                    cached.details().subject(), cached.details().usuario(), cached.details().expiresAt());
            return cached.details();
        }

        log.debug("Token no encontrado en cache o expirado, llamando a SAA");
        callValidationEndpoint(normalized);
        final Map<String, Object> claims = decodeClaims(normalized);
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:25 UTC-5 (Lima)][desc: Log seguro de claims decodificados sin valores sensibles][obj: SaaTokenService.validate claims keys]
        log.info("Claims decodificados (keys)={}", claims.keySet());
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:25 UTC-5 (Lima)][desc: Log seguro de valores claves del token][obj: SaaTokenService.validate claims preview]
        log.info("Claims resumen sub={} usuario={} idUsuaSist={}",
                readString(claims, "sub"),
                readString(claims, "Usuario"),
                readString(claims, "idUsuaSist"));
        final Instant expiresAt = resolveExpiration(claims)
                .orElseGet(() -> {
                    Instant fallback = Instant.now().plus(properties.getCache().getFallbackTtl());
                    log.debug("Sin claim exp, usando fallback {}", fallback);
                    return fallback;
                });
        final List<String> permisos = extractPermisos(claims);
        final SaaTokenDetails details = new SaaTokenDetails(
                normalized,
                readString(claims, "sub"),
                readString(claims, "Usuario"),
                resolveEmail(claims),
                expiresAt,
                permisos,
                claims
        );
        cache.put(normalized, new CachedToken(details, Instant.now()));
        log.info("Token validado y almacenado en cache para sub={} exp={}\n", details.subject(), expiresAt);
        return details;
    }

    private void callValidationEndpoint(String token) {
        URI uri = properties.getValidateUrl();
        if (uri == null) {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Evita fail-open si no hay validate-url; solo permite bypass explícito por config][obj: SaaTokenService.callValidationEndpoint]
            if (properties.isAllowUnsafeJwtDecode()) {
                log.warn("SAA validate URL no configurada; allowUnsafeJwtDecode=true, se omitirá validación remota");
                return;
            }
            throw new InvalidTokenException("SAA validate URL no configurada");
        }
        Map<String, String> payload = new java.util.HashMap<>();
        payload.put("token", token);
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: No loguea el token completo; solo una huella corta][obj: SaaTokenService.callValidationEndpoint]
        log.debug("Validando token SAA (preview={}): {}", previewToken(token), uri);
        try {
            RequestEntity<Map<String, String>> request = RequestEntity
                    .post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload);
            log.debug("Request hacia SAA armado: {} {}", request.getMethod(), request.getUrl());
            var response = restTemplate.exchange(request, Map.class);
            log.debug("Respuesta de validación SAA (status={})", response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {
                Object body = response.getBody();
                if (body instanceof Map<?, ?> map) {
                    Object result = map.get("resultado");
                    if (result instanceof String str && "VALIDO".equalsIgnoreCase(str.trim())) {
                        log.debug("SAA confirmó token válido");
                        return;
                    }
                    log.warn("SAA devolvió resultado no válido: {}", result);
                } else {
                    log.warn("Respuesta SAA inesperada (tipo={}): {}", body != null ? body.getClass() : "null", body);
                }
                throw new InvalidTokenException("Token inválido según SAA");
            }
            throw new InvalidTokenException("Error validando token con SAA (status %s)".formatted(response.getStatusCode()));
        } catch (RestClientException ex) {
            log.error("Excepción llamando a SAA", ex);
            throw new InvalidTokenException("Error validando token con SAA", ex);
        }
    }

    private String previewToken(String token) {
        if (token == null) return "null";
        String trimmed = token.trim();
        if (trimmed.length() <= 16) return trimmed;
        return trimmed.substring(0, 8) + "…" + trimmed.substring(trimmed.length() - 6);
    }

    private Map<String, Object> decodeClaims(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidTokenException("Token JWT inválido");
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            return objectMapper.readValue(decoded, MAP_TYPE);
        } catch (Exception ex) {
            throw new InvalidTokenException("No se pudo decodificar el payload del token", ex);
        }
    }

    private Optional<Instant> resolveExpiration(Map<String, Object> claims) {
        Object exp = claims.get("exp");
        if (exp instanceof Number number) {
            long epochSeconds = number.longValue();
            return Optional.of(Instant.ofEpochSecond(epochSeconds));
        }
        if (exp instanceof String str) {
            try {
                long epochSeconds = Long.parseLong(str);
                return Optional.of(Instant.ofEpochSecond(epochSeconds));
            } catch (NumberFormatException ignored) {
            }
        }
        return Optional.empty();
    }

    private List<String> extractPermisos(Map<String, Object> claims) {
        Object perfilPermiso = claims.get("PerfilPermiso");
        if (!(perfilPermiso instanceof List<?> perfiles)) {
            return List.of();
        }
        List<String> result = new java.util.ArrayList<>();
        for (Object perfil : perfiles) {
            if (!(perfil instanceof Map<?, ?> perfilMap)) {
                continue;
            }
            Object arrPermisos = perfilMap.get("arrPermisos");
            if (!(arrPermisos instanceof List<?> permisosList)) {
                continue;
            }
            for (Object permiso : permisosList) {
                if (!(permiso instanceof Map<?, ?> permisoMap)) {
                    continue;
                }
                addIfPresent(result, permisoMap.get("noAccion"));
                addIfPresent(result, permisoMap.get("idPermiso"));
                addIfPresent(result, permisoMap.get("noPermiso"));
            }
        }
        return result.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private void addIfPresent(List<String> target, Object value) {
        if (value instanceof String str && !str.isBlank()) {
            target.add(str);
        }
    }

    private String resolveEmail(Map<String, Object> claims) {
        String email = readString(claims, "email");
        if (email != null) {
            return email;
        }
        return readString(claims, "Email");
    }

    private String readString(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof String str && !str.isBlank()) {
            return str;
        }
        return null;
    }

    private String padBase64(String value) {
        int remainder = value.length() % 4;
        if (remainder == 0) {
            return value;
        }
        return value + "=".repeat(4 - remainder);
    }

    private record CachedToken(SaaTokenDetails details, Instant cachedAt) {
        boolean isStillValid(Duration minTtl) {
            Instant expiresAt = details.expiresAt();
            if (expiresAt == null) {
                return false;
            }
            Instant threshold = expiresAt.minus(minTtl);
            return Instant.now().isBefore(threshold);
        }
    }
}
