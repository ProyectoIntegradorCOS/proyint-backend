package pe.gob.onp.thaqhiri.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onp.thaqhiri.dto.CuestionarioDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.entity.Cuestionario;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.repository.CuestionarioRepository;
import pe.gob.onp.thaqhiri.util.UConstante;

import java.util.List;
import java.util.Optional;

@Service
public class CuestionarioService {

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 12:26 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioService logger]
    private static final Logger log = LoggerFactory.getLogger(CuestionarioService.class);

    @Autowired
    private CuestionarioRepository repository;
    @Autowired
    private PreguntaService preguntaService;

    // Buscar con paginación
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 12:43 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioService.buscar transaccional]
    @Transactional(readOnly = true)
    public Page<CuestionarioDTO> buscar(String nombre, int page, int size) {
    	
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 12:14 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioService.buscar orden]
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        // Obtenemos la página de entidades
        Page<Cuestionario> pageEntidades = repository.buscarPaginado(nombre, pageable);
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 12:26 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioService.buscar log]
        log.info("Buscar cuestionarios nombre='{}' page={} size={} total={} items={}",
                nombre,
                page,
                size,
                pageEntidades.getTotalElements(),
                pageEntidades.getNumberOfElements());
        
        // Convertimos cada entidad a DTO usando el método toDTO
        Page<CuestionarioDTO> pageDTO = pageEntidades.map(this::toDTO); 
        
        return pageDTO;
    }

    // Crear cuestionario
    public CuestionarioDTO crear(CuestionarioDTO dto, String usuario, String terminal){
        Cuestionario c = new Cuestionario();
        c.setNombre(dto.getNombre().trim().toUpperCase());
        
        if(dto.getDescripcion() != null) {
        	c.setDescripcion(dto.getDescripcion().trim().toUpperCase());
        }
        
        c.setEstado(dto.getEstado() != null ? dto.getEstado() : UConstante.ACTIVO_REGI);
        c.setUsuarioCrea(usuario);
        c.setTerminalCrea(terminal);

        Cuestionario saved = repository.save(c);

        return toDTO(saved);
    }

    // Actualizar cuestionario
    public CuestionarioDTO actualizar(Long id, CuestionarioDTO dto, String usuario, String terminal){
        Optional<Cuestionario> optional = repository.findById(id);
        if(optional.isPresent()){
            Cuestionario c = optional.get();
            c.setNombre(dto.getNombre().trim().toUpperCase());
            
            if(dto.getDescripcion() != null) {
            	c.setDescripcion(dto.getDescripcion().trim().toUpperCase());
            }
            
            c.setEstado(dto.getEstado() != null ? dto.getEstado() : UConstante.ACTIVO_REGI);
            c.setUsuarioModi(usuario);
            c.setTerminalModi(terminal);

            Cuestionario saved = repository.save(c);            

            return toDTO(saved);
        }
        throw new RuntimeException("Cuestionario no encontrado");
    }

    // Eliminar cuestionario
    public void eliminar(Long id, String usuario, String terminal){
    	
        Optional<Cuestionario> optional = repository.findById(id);
        
        if (optional.isPresent()) {
            Cuestionario cuestionario = optional.get();
            
            // Validar que no se pueda eliminar un cuestionario si tiene equipos asociados
            if (repository.existeAsignacionAEquipo(id)) {
                throw new RuntimeException("No se puede eliminar el cuestionario mientras esté asociado a un equipo de trabajo.");
            }
            
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 11:45 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioService.eliminar]
            preguntaService.desactivarPorCuestionario(id, usuario, terminal);
            
            repository.desactivar(id, usuario, terminal);
            
            return;
        }
        throw new RuntimeException("Cuestionario no encontrado");
    }
    
    public CuestionarioDTO toDTO(Cuestionario entidad) {
    	
    	CuestionarioDTO dto = new CuestionarioDTO();
    	
    	dto.setDescripcion(entidad.getDescripcion());
    	dto.setEstado(entidad.getEstado());
    	dto.setId(entidad.getId());
    	dto.setNombre(entidad.getNombre());    	
        
    	return dto;
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 13:20 UTC-5 (Lima)][desc: Resuelve lazy equipo y devuelve DTO dentro de transacción][obj: CuestionarioService.obtenerActivoPorEquipo]
    @Transactional(readOnly = true)
    public Optional<CuestionarioDTO> obtenerActivoPorEquipo(Long equipoId) {
    	
        return repository.buscarActivoPorEquipo(equipoId)
                            .map(this::toDTO);
    }
    
    
    public List<CuestionarioDTO> listarCuestionarios() {
        // Usamos la consulta generada por Spring Data JPA, buscando por estado = 1
        List<Cuestionario> activos = repository.findByEstadoOrderByNombre(UConstante.ACTIVO);

        // Mapea la entidad Colaborador a ColaboradorSimpleDTO
        return activos.stream()
                .map(this::toDTO)
                .toList(); 
    }
    
    public CuestionarioDTO obtener(Integer idCuestionario) {
    	
    	Optional<Cuestionario> oCuestionario = repository.findById(Long.valueOf(idCuestionario));
    	
    	if(oCuestionario.isPresent()) {
    		return toDTO(oCuestionario.get());
    	}
    	else {
    		return null;
    	}
    }
    
}
