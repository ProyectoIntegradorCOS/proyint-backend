package pe.gob.onp.thaqhiri.auth;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaaPrincipal implements Principal {
	private static final Logger log = LoggerFactory.getLogger(SaaPrincipal.class);

    private final SaaTokenDetails details;
    private final List<GrantedAuthority> authorities;

    public SaaPrincipal(SaaTokenDetails details) {
        this.details = details;
        this.authorities = Collections.unmodifiableList(
                details.permisos().stream()
                        .filter(p -> p != null && !p.isBlank())
                        .map(String::trim)
                        .map(p -> p.replace(' ', '_').toUpperCase())
                        .distinct()
                        .map(SimpleGrantedAuthority::new)
                        .map(authority -> (GrantedAuthority) authority)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public String getName() {
        return details.subject();
    }

    public String getUsuario() {
        return details.usuario();
    }

    public String getEmail() {
        return details.email();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public SaaTokenDetails getDetails() {
        log.info("SaaPrincipal - details: "+details);
        
        return details;
    }
}
