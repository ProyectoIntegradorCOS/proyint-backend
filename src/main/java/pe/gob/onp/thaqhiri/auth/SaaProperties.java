package pe.gob.onp.thaqhiri.auth;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "saa")
public class SaaProperties {

	// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Agrega URL configurable para búsqueda de usuario en SAA usada por UserController][obj: SaaProperties.buscarUsuarioUrl]
	private String buscarUsuarioUrl;
	private String generateUrl;
	// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-09 11:10 UTC-5 (Lima)][desc: URL para generar token por usuario/clave (SAA token/usuario/generar)][obj: SaaProperties.usuarioGenerateUrl]
	private String usuarioGenerateUrl;
	private String renewUrl;
	private String closeUrl;
    private URI validateUrl;
    private String perfilSistemaUrl;
    private String usuarioPerfilVigenteUrl;
    private String perfilCampo;
    private String systemCodeThaqhiri;
    
    private boolean allowUnsafeJwtDecode = false;
    private Duration timeout = Duration.ofSeconds(5);
    private final Cache cache = new Cache();

    public URI getValidateUrl() {
        return validateUrl;
    }

    public void setValidateUrl(URI validateUrl) {
        this.validateUrl = validateUrl;
    }


    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public boolean isAllowUnsafeJwtDecode() {
        return allowUnsafeJwtDecode;
    }

    public void setAllowUnsafeJwtDecode(boolean allowUnsafeJwtDecode) {
        this.allowUnsafeJwtDecode = allowUnsafeJwtDecode;
    }

    public Cache getCache() {
        return cache;
    }
    
    
	public String getBuscarUsuarioUrl() {
		return buscarUsuarioUrl;
	}

	public void setBuscarUsuarioUrl(String buscarUsuarioUrl) {
		this.buscarUsuarioUrl = buscarUsuarioUrl;
	}

    public String getGenerateUrl() {
		return generateUrl;
	}

	public void setGenerateUrl(String generateUrl) {
		this.generateUrl = generateUrl;
	}

	public String getUsuarioGenerateUrl() {
		return usuarioGenerateUrl;
	}

	public void setUsuarioGenerateUrl(String usuarioGenerateUrl) {
		this.usuarioGenerateUrl = usuarioGenerateUrl;
	}

	public String getRenewUrl() {
		return renewUrl;
	}

	public void setRenewUrl(String renewUrl) {
		this.renewUrl = renewUrl;
	}



	public static class Cache {
        private Duration minTtl = Duration.ofMinutes(1);
        private Duration fallbackTtl = Duration.ofMinutes(5);

        public Duration getMinTtl() {
            return minTtl;
        }

        public void setMinTtl(Duration minTtl) {
            this.minTtl = minTtl;
        }

        public Duration getFallbackTtl() {
            return fallbackTtl;
        }

        public void setFallbackTtl(Duration fallbackTtl) {
            this.fallbackTtl = fallbackTtl;
        }
    }



	public String getCloseUrl() {
		return closeUrl;
	}

	public void setCloseUrl(String closeUrl) {
		this.closeUrl = closeUrl;
	}

	public String getPerfilSistemaUrl() {
		return perfilSistemaUrl;
	}

	public void setPerfilSistemaUrl(String perfilSistemaUrl) {
		this.perfilSistemaUrl = perfilSistemaUrl;
	}

	public String getUsuarioPerfilVigenteUrl() {
		return usuarioPerfilVigenteUrl;
	}

	public void setUsuarioPerfilVigenteUrl(String usuarioPerfilVigenteUrl) {
		this.usuarioPerfilVigenteUrl = usuarioPerfilVigenteUrl;
	}

	public String getPerfilCampo() {
		return perfilCampo;
	}

	public void setPerfilCampo(String perfilCampo) {
		this.perfilCampo = perfilCampo;
	}

	public String getSystemCodeThaqhiri() {
		return systemCodeThaqhiri;
	}

	public void setSystemCodeThaqhiri(String systemCodeThaqhiri) {
		this.systemCodeThaqhiri = systemCodeThaqhiri;
	}

	
		
}
