package pe.gob.onp.thaqhiri.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.entity.Destino;
import pe.gob.onp.thaqhiri.repository.DestinoRepository;

@Service
public class DestinoImportBatchService {

	private static final Logger log = LoggerFactory.getLogger(DestinoImportBatchService.class);
	
    private final DestinoRepository destinoRepository;

    public DestinoImportBatchService(DestinoRepository destinoRepository) {
        this.destinoRepository = destinoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarLote(List<Destino> lote) {
        destinoRepository.saveAll(lote);
        destinoRepository.flush();
        
        log.info("guardarLote() ejecutado");
    }
}