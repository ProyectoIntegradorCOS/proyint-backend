package pe.gob.onp.thaqhiri.auth;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record SaaTokenDetails(
        String token,
        String subject,
        String usuario,
        String email,
        Instant expiresAt,
        List<String> permisos,
        Map<String, Object> claims
) {

    public SaaTokenDetails {
        permisos = permisos == null ? List.of() : List.copyOf(permisos);
        claims = claims == null ? Map.of() : Collections.unmodifiableMap(claims);
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
}
