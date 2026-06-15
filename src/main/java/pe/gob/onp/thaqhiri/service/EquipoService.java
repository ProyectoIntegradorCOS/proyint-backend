package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.entity.Equipo;
import pe.gob.onp.thaqhiri.dto.CuestionarioDTO;
import pe.gob.onp.thaqhiri.dto.EquipoDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.repository.EquipoRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.repository.UserRepository;
import pe.gob.onp.thaqhiri.service.EquipoService;

import java.util.Optional;

/**
 * Servicio que implementa la lógica de negocio para la gestión de Equipos.
 */
@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CuestionarioService cuestionarioService;
    
    /**
     * Obtiene una lista de todos los equipos activos, ordenados por nombre.
     * @return Lista de entidades Equipo activas.
     */
    @Transactional(readOnly = true)
    public List<EquipoDTO> listarActivos() {
        return equipoRepository.findByEstadoOrderByNombreAsc(UConstante.ACTIVO_REGI)
            .stream()
            .map(equipo -> toResponse(equipo))
            .collect(Collectors.toList());
    }
    
    
    @Transactional(readOnly = true)
    public List<EquipoDTO> obtenerEquiposSupervisados(Long idSupervisor) {
        
        return equipoRepository.findBySupervisorOrderByNombreAsc(idSupervisor)
                .stream()
                .map(equipo -> toResponse(equipo))
                .collect(Collectors.toList());
    }
    
    
    @Transactional(readOnly = true)
    public RespuestaBusquedaDTO<EquipoDTO> buscarPaginado(String nombreEquipo,
    		                              String supervisorNombre,
                                          int pagina, 
                                          int tamanioPagina,
                                          String orden, 
                                          String columnaOrden) {
    	
    	RespuestaBusquedaDTO<EquipoDTO> respuesta = new RespuestaBusquedaDTO<>();
    	
        //Determinar el ordenamiento
        Sort sort = UConstante.ORDEN_DESCENDENTE.equalsIgnoreCase(orden)
                ? Sort.by(columnaOrden).descending()
                : Sort.by(columnaOrden).ascending();

        //La paginación en JPA es 0-indexada, por ello se resta 1 al numero de pagina recibido.
        Pageable pageable = PageRequest.of(pagina - 1, tamanioPagina, sort);
        Page<Equipo> pageResult = null;
        
        //Filtros
        String nombreEquipoBusqueda = nombreEquipo;
        String supervisorNombreBusqueda = supervisorNombre;
        
        if(nombreEquipo != null && nombreEquipo.trim().equals("")) {
        	nombreEquipoBusqueda = null; 
        }
        
        if(supervisorNombre != null && supervisorNombre.trim().equals("")) {
        	supervisorNombreBusqueda = null;
        }
        
        //Busca por nombres y equipo (2 filtros)
    	pageResult = equipoRepository.buscarEquiposPaginado(nombreEquipoBusqueda, supervisorNombreBusqueda, pageable);
    	
    	long totalRegistros = pageResult.getTotalElements();
    	
    	Page<EquipoDTO> resultado = pageResult.map(this::toResponse);    	
    	
    	if (resultado.getContent().isEmpty()) {
    		respuesta.setCodigoResultado(UConstante.RESULTADO_NO_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("No se encontraron colaboradores con los criterios especificados.");
    		respuesta.setResultados(List.of());
    		respuesta.setTotalPaginas(0);    
    		respuesta.setTotalRegistros(0);
    	} 
    	else {
    		respuesta.setCodigoResultado(UConstante.RESULTADO_SI_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("Búsqueda exitosa.");
    		respuesta.setResultados(resultado.getContent()); // Contenido de la página actual
    		respuesta.setTotalPaginas(resultado.getTotalPages()); // Total de páginas
    		respuesta.setPaginaActual(pagina);
    		respuesta.setTamanioPagina(tamanioPagina);
    		respuesta.setTotalRegistros(totalRegistros);
    	}
    	
    	return respuesta;    	  
    }
    
    
    private EquipoDTO toResponse(Equipo equipo) {
    	
    	Long supervisorId = null;
    	String supervisorNombre = null;    	
    	
    	if(equipo.getSupervisor() != null) {
    		supervisorId = equipo.getSupervisor().getId();
    		supervisorNombre = equipo.getSupervisor().getNombre();
    	}    	
    	
    	CuestionarioDTO dtoCuestionario = cuestionarioService.obtener(equipo.getIdCuestionario());
    	String nombreCuestionario = dtoCuestionario != null ? dtoCuestionario.getNombre() : "";
    	
    	EquipoDTO respuesta = new EquipoDTO(
    			equipo.getId(),
    			equipo.getNombre(),
    			supervisorId,
    			supervisorNombre,
    			equipo.getRealizaVisitas() == UConstante.ACTIVO ? true : false,
    			equipo.getIdCuestionario(),
    			nombreCuestionario
        );
    	
    	return respuesta;
    }
    
    @Transactional
    public EquipoDTO registrar(EquipoDTO dto, String usuario, String terminal) {

        Equipo entidad = new Equipo();
        entidad.setNombre(dto.getNombre().trim().toUpperCase());
        entidad.setIdCuestionario(dto.getIdCuestionario());
        entidad.setEstado(UConstante.ACTIVO_REGI);
        entidad.setUsuarioCreacion(usuario);
        entidad.setTerminalCreacion(terminal);        
        
        if(dto.getRealizaVisitas() != null) {
        	entidad.setRealizaVisitas(dto.getRealizaVisitas() ? UConstante.ACTIVO : UConstante.INACTIVO);
        }
        
        if(dto.getSupervisorId() != null && dto.getSupervisorId() > 0) {
        	User supervisor = new User();
        	supervisor.setId(dto.getSupervisorId());
        	entidad.setSupervisor(supervisor);
        }

        // Guarda
        Equipo guardado = equipoRepository.save(entidad);
        
        // Mapea DTO de salida
        return toResponse(guardado);
    }
    
    @Transactional
    public EquipoDTO actualizar(EquipoDTO dto, String usuario, String terminal) {

        Equipo entidad = equipoRepository.findById(Long.valueOf(dto.getId()))
                .orElseThrow(() -> new RuntimeException("Equipo no existe"));
        
        entidad.setNombre(dto.getNombre().trim().toUpperCase());    
        entidad.setIdCuestionario(dto.getIdCuestionario());
        entidad.setEstado(UConstante.ACTIVO_REGI);
        entidad.setUsuarioModificacion(usuario);
        entidad.setTerminalModificacion(terminal);
        
        if(dto.getRealizaVisitas() != null) {
        	entidad.setRealizaVisitas(dto.getRealizaVisitas() ? UConstante.ACTIVO : UConstante.INACTIVO);
        }
        
        if(dto.getSupervisorId() != null && dto.getSupervisorId() > 0) {
        	User supervisor = new User();
        	supervisor.setId(dto.getSupervisorId());
        	entidad.setSupervisor(supervisor);
        }

        //Guarda
        Equipo actualizado = equipoRepository.save(entidad);

        //Evalua
        Long idSupervisorActualizado = null;
        String supervisorNombre = null;

        if(actualizado.getSupervisor() != null) {
        	idSupervisorActualizado = actualizado.getSupervisor().getId();
        	supervisorNombre = actualizado.getSupervisor().getNombre();
        }
        
        return toResponse(actualizado);
    }
    
    /**
     * Elimina un equipo.
     * @param id
     * @param usuarioSesion
     * @param terminalSesion
     * @return Retorna null en caso de éxito, en otro caso devuelve un mensaje. 
     */
    @Transactional
    public String eliminar(Long id, String usuarioSesion, String terminalSesion) {

    	//Validar que el equipo exista y esté activo 
        if (!equipoRepository.existsByIdAndEstado(id, UConstante.ACTIVO_REGI)) {
            return "El equipo de trabajo debe existir y estar activo.";
        }
        
        //Validar que el equipo no tenga personal asignado
        if(!userRepository.findByEquipoIdAndEstadoAndTipoTrabajo(id, UConstante.ACTIVO_REGI, UConstante.TIPO_TRABAJO_CAMPO).isEmpty()) {
        	return "El equipo de trabajo no debe tener Colaboradores asignados.";
        }

        //Eliminar
        int filas = equipoRepository.eliminarEquipo(id, usuarioSesion, terminalSesion);
        
        return filas > 0 ? null : "No se pudo eliminar el equipo de trabajo indicado.";
    }
    
    
    /**
     * Obtiene una lista de todos los equipos activos, buscando por coincidencia exacta del nombre.
     * @return Lista de entidades Equipo activas.
     */
    @Transactional(readOnly = true)
    public List<EquipoDTO> buscarPorNombre(String nombre) {
        return equipoRepository.findByNombreAndEstado(nombre.trim().toUpperCase(), UConstante.ACTIVO_REGI)
            .stream()
            .map(equipo -> toResponse(equipo))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene una lista de todos los equipos activos, buscando por coincidencia exacta del nombre.
     * @return Lista de entidades Equipo activas.
     */
    @Transactional(readOnly = true)
    public List<EquipoDTO> buscarPorNombreOtroId(String nombre, Integer id) {
        return equipoRepository.findByNombreAndIdNot(nombre.trim().toUpperCase(), id)
            .stream()
            .map(equipo -> toResponse(equipo))
            .collect(Collectors.toList());
    }

}
