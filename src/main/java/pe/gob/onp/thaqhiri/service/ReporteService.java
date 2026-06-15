package pe.gob.onp.thaqhiri.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.gob.onp.thaqhiri.dto.ReporteProductividadDTO;
import pe.gob.onp.thaqhiri.model.VisitItemState;
import pe.gob.onp.thaqhiri.repository.ReporteRepository;
import pe.gob.onp.thaqhiri.service.ReporteService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class ReporteService {

	@Autowired
    private ReporteRepository reportRepo;

    public List<ReporteProductividadDTO> obtenerReporte(String idPersona, LocalDate fechaInicio, LocalDate fechaFin) {
    	
    	//Pasar la lista de ids de personas a una coleccion
		Collection<Long> ids = null;

	    if (idPersona != null && !idPersona.isBlank()) {
	        ids = Arrays.stream(idPersona.split(","))
	                .map(String::trim)
	                .map(Long::valueOf)
	                .toList();
	    }
	    
        return reportRepo.obtenerReporteProductividad(ids, fechaInicio, fechaFin, VisitItemState.DELETED);
    }
}
