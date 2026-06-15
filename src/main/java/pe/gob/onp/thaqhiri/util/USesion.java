package pe.gob.onp.thaqhiri.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;

public class USesion {
	
	public static String determineHost(HttpServletRequest request) {
		
		//Considera que el backend puede estar detras de un proxy / balanceador
	    String xff = request.getHeader("X-Forwarded-For");
	    
	    if (xff != null && !xff.isBlank()) {
	        // Puede venir: "IP1, IP2, IP3"
	        return xff.split(",")[0].trim();
	    }
	    
	    //Si no, de esta forma obtiene la ip remota.
	    return request.getRemoteAddr();
	}
	
	

	
	public static String resolveUsuario() {
		
		SaaPrincipal principal = requirePrincipal();
				
        if (principal == null) {
            return "USUARIO_SESION";
        }
        
        String usuario = principal.getUsuario();
        if (usuario != null && !usuario.isBlank()) {
            return usuario;
        }
        
        String subject = principal.getName();
        return subject != null && !subject.isBlank() ? subject : "USUARIO_SESION";
    }
	
	public static SaaPrincipal requirePrincipal() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SaaPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no presente");
        }
        return principal;
    }
	
	

}
