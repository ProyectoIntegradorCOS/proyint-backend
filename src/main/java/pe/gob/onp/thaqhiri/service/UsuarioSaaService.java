package pe.gob.onp.thaqhiri.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.UsuarioSaaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsuarioSaaService {

	private static final Logger log = LoggerFactory.getLogger(UsuarioSaaService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public RespuestaDTO<UsuarioSaaDTO> buscarUsuarioSaa(String url, String idSistema, String usuario) {
    	
    	String urlBuscar = url + "?idSistema=" + idSistema + "&login=" + usuario;

        try {
            ResponseEntity<RespuestaDTO<UsuarioSaaDTO>> response =
                    restTemplate.exchange(
                    		urlBuscar,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {}
                    );

            return response.getBody();

        } catch (Exception e) {
        	log.info("ERROR al invocar servicio Saa: " + e.getMessage());
            return null;
        }
    }
}
