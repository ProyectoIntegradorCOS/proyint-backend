package pe.gob.onp.thaqhiri.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pe.gob.onp.thaqhiri.dto.UsuarioPerfilDTO;
import pe.gob.onp.thaqhiri.service.SincronizacionService;

@Component
@EnableScheduling
public class UTareaAutomaticaScheduler {

    private static final Logger log = LoggerFactory.getLogger(UTareaAutomaticaScheduler.class);
    
    @Autowired
    SincronizacionService sincronizacionService;

    /**
     * Proceso periódico.
     * La frecuencia se define en application.yaml
     */
    @Scheduled(fixedDelayString = "${tarea.automatica.delay:1h}")
    public void ejecutarProceso() {
    	
        log.info("Inicio de la tarea automática");

        try {
        	procesarTareaInactivarUsuarios();
        }
        catch (Exception e) {
            log.error("Error durante la tarea automática", e);
        }

        log.info("Fin de la tarea automática \n");
    }

    
    /**
     * Lógica real del negocio.
     * Debe ser idempotente.
     */
    private void procesarTareaInactivarUsuarios() {
    	
    	log.info("Inicio procesarTareaInactivarUsuarios()");
    	String usuario = "TareaAuto";
    	String terminal = "TareaAuto";
    	
    	try {	
	        //Lista de usuarios vigentes del sistema en el SAA 
	        log.info("Vamos a recuperar la información del SAA");
            List<UsuarioPerfilDTO> usuariosActivosSaaSinDuplicados = sincronizacionService.obtenerUsuariosSaaSinDuplicados();

	        sincronizacionService.inactivarUsuariosNoVigentes(usuariosActivosSaaSinDuplicados, usuario, terminal);	        
    	}
    	catch (Exception e) {
            log.error("Error durante la ejecucion del metodo procesarTareaInactivarUsuarios()", e);
        }
    	
    	log.info("Fin procesarTareaInactivarUsuarios()");
    }
}
