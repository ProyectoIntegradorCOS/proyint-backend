package pe.gob.onp.thaqhiri.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SaaAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SaaAuthenticationFilter.class);

    private final SaaTokenService tokenService;
    private final SaaAuthenticationEntryPoint entryPoint;

    public SaaAuthenticationFilter(SaaTokenService tokenService, SaaAuthenticationEntryPoint entryPoint) {
        this.tokenService = tokenService;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.debug("Procesando request {} {}", request.getMethod(), request.getRequestURI());
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            log.debug("Header Authorization no presente o no es Bearer, se continúa sin autenticar");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            log.trace("Token recibido, iniciando validación contra SAA");
            SaaTokenDetails details = tokenService.validate(token);
            SaaPrincipal principal = new SaaPrincipal(details);
            log.debug("Token SAA válido para sub={} usuario={}", principal.getName(), principal.getUsuario());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (InvalidTokenException ex) {
            log.warn("Token SAA inválido: {}, token: " + token, ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response,
                    new org.springframework.security.authentication.BadCredentialsException(ex.getMessage(), ex));
            return;
        }

        log.trace("Autenticación SAA almacenada en SecurityContext, continuando cadena de filtros");
        filterChain.doFilter(request, response);
    }
}
