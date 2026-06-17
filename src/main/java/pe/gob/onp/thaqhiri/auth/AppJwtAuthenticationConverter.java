package pe.gob.onp.thaqhiri.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AppJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        SaaTokenDetails details = new SaaTokenDetails(
                jwt.getTokenValue(),
                jwt.getSubject(),
                jwt.getClaimAsString("Usuario"),
                jwt.getClaimAsString("email"),
                jwt.getExpiresAt(),
                List.of(),
                jwt.getClaims() != null ? Map.copyOf(jwt.getClaims()) : Map.of()
        );
        SaaPrincipal principal = new SaaPrincipal(details);
        return new UsernamePasswordAuthenticationToken(
                principal, jwt.getTokenValue(), principal.getAuthorities());
    }
}
