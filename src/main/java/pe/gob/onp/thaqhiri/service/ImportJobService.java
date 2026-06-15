package pe.gob.onp.thaqhiri.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import pe.gob.onp.thaqhiri.dto.ImportJobStatusDTO;
import pe.gob.onp.thaqhiri.entity.ImportJob;
import pe.gob.onp.thaqhiri.exception.BusinessException;
import pe.gob.onp.thaqhiri.repository.ImportJobRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ImportJobService {

	private static final Logger log = LoggerFactory.getLogger(ImportJobService.class);
	
    private final ImportJobRepository repository;

    public ImportJobService(ImportJobRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Long crearJob(int totalFilas, String usuarioSesion, String terminalSesion) {

    	log.info("crearJob(), Inicio, totalFilas=" + totalFilas);
    	
        ImportJob job = new ImportJob();
        job.setTotalFilas(totalFilas);
        job.setFilasProcesadas(0);
        job.setPorcentaje(0);
        job.setEstado("PROCESANDO");
        job.setFechaInicio(LocalDateTime.now());
        job.setUsuarioCreacion(usuarioSesion);
        job.setTerminalCreacion(terminalSesion);

        ImportJob entity = repository.save(job);

        log.info("crearJob(), Fin, jobId=" + entity.getId());
        
        return entity.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actualizarProgreso(Long jobId, int procesadas) {

    	//log.info("actualizarProgreso(), Inicio, jobId=" + jobId + ", procesadas=" + procesadas);
    	
    	long horasRestantes = 0;
        long minutosRestantes = 0;
        long segundosRestantes = 0;
        
        ImportJob job = repository.findById(jobId).orElseThrow();

        job.setFilasProcesadas(procesadas);

        int porcentaje = (int) ((procesadas * 100.0) / job.getTotalFilas());
        job.setPorcentaje(Math.min(porcentaje, 100));

        //Calcula el tiempo estimado restante
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicio = job.getFechaInicio();

        if (inicio != null && procesadas > 0) {

            // Tiempo transcurrido en segundos
            long segundosTranscurridos = Duration.between(inicio, ahora).getSeconds();

            if (segundosTranscurridos > 0) {

                // Velocidad promedio (filas por segundo)
                double filasPorSegundo = (double) procesadas / segundosTranscurridos;

                // Filas restantes
                int filasRestantes = job.getTotalFilas() - procesadas;

                // Segundos estimados restantes
                long segundosFaltantesTotal = (long) (filasRestantes / filasPorSegundo);

                // Convertir a horas, minutos y segundos
                horasRestantes = segundosFaltantesTotal / 3600;
                minutosRestantes = (segundosFaltantesTotal % 3600) / 60;
                segundosRestantes = segundosFaltantesTotal % 60;

                //log.info("Tiempo transcurrido: {} segundos", segundosTranscurridos);
                //log.info("Tiempo estimado restante: {}h {}m {}s", horasRestantes, minutosRestantes, segundosRestantes);
            }
        }
        
        job.setNuHorasRestantes(horasRestantes);
        job.setNuMinutosRestantes(minutosRestantes);
        job.setNuSegundosRestantes(segundosRestantes);
        
        repository.save(job);
        
        log.info("actualizarProgreso(), Fin, jobId=" + jobId + ", procesadas=" + procesadas + ", porcentaje=" + porcentaje);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completar(Long jobId, String mensaje) {

    	//log.info("completar(), Inicio, jobId=" + jobId + ", mensaje=" + mensaje);
    	
        ImportJob job = repository.findById(jobId).orElseThrow();

        job.setEstado("COMPLETADO");
        job.setPorcentaje(100);
        job.setMensaje(mensaje);
        job.setFechaFin(LocalDateTime.now());

        repository.save(job);
        
        log.info("completar(), Fin, jobId=" + jobId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void error(Long jobId, String mensaje) {
    	
    	//log.info("error(), Inicio, jobId=" + jobId + ", mensaje=" + mensaje);

        ImportJob job = repository.findById(jobId).orElseThrow();

        job.setEstado("ERROR");
        job.setMensaje(mensaje);
        job.setFechaFin(LocalDateTime.now());

        repository.save(job);
        
        log.info("error(), Fin, jobId=" + jobId);
    }
    
    @Transactional(readOnly = true)
    public ImportJobStatusDTO obtener(Long jobId) {
    	
    	//log.info("obtener(), Inicio, jobId=" + jobId);

        ImportJob job = repository.findById(jobId)
                .orElseThrow(() ->
                        new BusinessException("No existe el job con id: " + jobId));

        /*
        log.info("obtener(), Inicio, jobId=" + jobId + ", getNuHorasRestantes()=" + job.getNuHorasRestantes() + 
        		", getNuMinutosRestantes()=" + job.getNuMinutosRestantes() +
        		", getNuSegundosRestantes()=" + job.getNuSegundosRestantes());
        */
        
        ImportJobStatusDTO dto = new ImportJobStatusDTO(
                job.getId(),
                job.getTotalFilas(),
                job.getFilasProcesadas(),
                job.getPorcentaje(),
                job.getEstado(),
                job.getMensaje(),
                job.getNuHorasRestantes(),
                job.getNuMinutosRestantes(),
                job.getNuSegundosRestantes()
        );
        
        log.info("obtener(), Fin, jobId=" + jobId + ", getFilasProcesadas=" + job.getFilasProcesadas());
        
        return dto;
    }
    
    
    @Transactional
    public void actualizarTotal(Long jobId, int total) {

        ImportJob job = repository.findById(jobId).orElseThrow();

        job.setTotalFilas(total);

        repository.save(job);
    }
    
    
}