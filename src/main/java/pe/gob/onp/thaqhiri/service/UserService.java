package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.UserRequest;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.entity.Equipo;
import pe.gob.onp.thaqhiri.entity.Horario;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import pe.gob.onp.thaqhiri.repository.EquipoRepository;
import pe.gob.onp.thaqhiri.repository.HorarioRepository;
import pe.gob.onp.thaqhiri.repository.UserRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final EquipoRepository equipoRepository;
    private final HorarioRepository horarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, EquipoRepository equipoRepository,
            HorarioRepository horarioRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.equipoRepository = equipoRepository;
        this.horarioRepository = horarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static String determineHost() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "APP_MOVIL";
        }
    }

    public UserResponse createOrUpdateBySaaSubject(UserRequest request, String usuario, String terminal) {
        log.debug("Procesando usuario {}", request.saaSubject());
        User user = userRepository
                .findBySaaSub(request.saaSubject())
                .orElseGet(User::new);

        return create(user, request, usuario, terminal);
    }
    
    public UserResponse createOrUpdateById(UserRequest request, String usuario, String terminal) {
        log.debug("Procesando usuario by Id {}", request.id());

        User user = null;
        
        if (request.id() == null || request.id() < 1) {
        	user = new User();
        }
        else {
        	user = userRepository
                    .findById(request.id())
                    .orElseThrow();

            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Evita que un usuario actualice por ID registros de terceros (validación de ownership por saaSub)][obj: UserService.createOrUpdateById]
            String existingSaaSub = user.getSaaSub();
            String requestedSaaSub = request.saaSubject();
            if (existingSaaSub != null
                    && requestedSaaSub != null
                    && !existingSaaSub.equals(requestedSaaSub)) {
                throw new AccessDeniedException("No autorizado para modificar el usuario con id=" + request.id());
            }
        }
        
        return create(user, request, usuario, terminal);
    }
    
    
    private UserResponse create(User user, UserRequest request, String usuario, String terminal) {
        log.debug("Procesando usuario {}", request.saaSubject());

        user.setSaaSub(request.saaSubject());
        user.setUsuario(request.usuario());
        user.setNombre(request.nombre());
        user.setEstado(request.estado() != null ? request.estado() : UConstante.ACTIVO);
        
        if (user.getId() == null) {
            user.setUsuarioCreacion(usuario);
            user.setTerminalCreacion(terminal);
        } else {
            user.setUsuarioModificacion(usuario);
            user.setTerminalModificacion(terminal);
        }
        
        Equipo equipo = null;
    	if(request.equipoId() != null) {    		
	        equipo = equipoRepository.findById(request.equipoId().longValue())
	            .orElseThrow(() -> new RuntimeException("Error: Equipo no encontrado con ID: " + request.equipoId()));
    	}
    	user.setEquipo(equipo);
    	
    	Horario horario = horarioRepository.findById(request.horarioId())
    			.orElseThrow(() -> new RuntimeException("Error: Horario no encontrado con ID: " + request.horarioId()));
    	user.setHorario(horario);
    	    	
        User saved = userRepository.save(user);
        log.debug("Usuario guardado con id={} saaSub={}", saved.getId(), saved.getSaaSub());
        
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getBySaaSubject(String saaSubject) {
        User user = userRepository.findBySaaSub(saaSubject)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para saaSub=" + saaSubject));
        log.debug("Usuario encontrado saaSub={}", saaSubject);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.debug("Recuperando lista completa de usuarios");
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public User getEntityBySaaSubject(String saaSubject) {
        return userRepository.findBySaaSub(saaSubject)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para saaSub=" + saaSubject));
    }
    
    @Transactional(readOnly = true)
    public User getEntityByUsuario(String usuario) {
        return userRepository.findByUsuarioAndEstado(usuario, UConstante.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para usuario=" + usuario));
    }

    @Transactional(readOnly = true)
    public User getEntity(Long id) {
        log.info("Como llegmos aquí???");
        final String caller = java.util.Arrays.stream(Thread.currentThread().getStackTrace())
                .skip(2).limit(6)
                .map(e -> e.getClassName().replaceAll(".*\\.", "") + "." + e.getMethodName() + ":" + e.getLineNumber())
                .collect(java.util.stream.Collectors.joining(" → "));
        log.info("getEntity(id={}) llamado desde: {}", id, caller);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para id=" + id));
    }

    private UserResponse toResponse(User user) {
    	
    	Integer idEquipo = null;
    	String equipoNombre = null;
    	String estadoDescripcion = null;
    	Long horarioId = null;
    	String horarioNombre = null;
    	
    	if(user.getEquipo() != null) {
    		idEquipo = user.getEquipo().getId();
    		equipoNombre = user.getEquipo().getNombre();
    	}
    	
    	if (user.getHorario() != null) {
    		horarioId = user.getHorario().getId();
    		horarioNombre = user.getHorario().getNombre();
    	}
    	
    	if(UConstante.ACTIVO.equals(user.getEstado())) {
    		estadoDescripcion = UConstante.ACTIVO_DESCRIPCION;
    	}
    	else {
    		estadoDescripcion = UConstante.INACTIVO_DESCRIPCION;
    	}

        Integer estadoNumerico = user.getEstado();
        UserResponse respuesta = new UserResponse(
                user.getId(),
                user.getSaaSub(),
                user.getUsuario(),
                user.getNombre(),
                estadoNumerico,
                estadoDescripcion,
                idEquipo,
                equipoNombre,
                horarioId,
                horarioNombre
        );
    	
    	return respuesta;
    }
    
    
    /**
     * Realiza la búsqueda de colaboradores con paginación.
     * @param nombre Filtro de búsqueda para los nombres, tipo String.
     * @param orden Tipo de ordenamiento, tipo String.
     * @param columnaOrden Nombre de la columna para el ordenamiento, tipo String.
     * @param pagina Número de página, tipo Integer.
     * @param tamanio Tamaño de la página en filas, tipo Integer.
     * @return Devuelve una pagina de datos, tipo Page<ColaboradorDTO>.
     */
    @Transactional(readOnly = true)
    public RespuestaBusquedaDTO<UserResponse> buscarUsuariosCampoPaginado(String nombre,
    		                                   String equipoId,
                                               int pagina, 
                                               int tamanioPagina,
                                               String orden, 
                                               String columnaOrden) {
    	
    	RespuestaBusquedaDTO<UserResponse> respuesta = new RespuestaBusquedaDTO<>();
    	
        //Determinar el ordenamiento
        List<String> validColumns = List.of("id", "nombre", "usuario", "estado", "equipoId", "horarioId", "saaSub", "equipo.nombre", "horario.nombre");
        String safeColumnaOrden = validColumns.contains(columnaOrden) ? columnaOrden : "id";

        Sort sort = UConstante.ORDEN_DESCENDENTE.equalsIgnoreCase(orden)
                ? Sort.by(safeColumnaOrden).descending()
                : Sort.by(safeColumnaOrden).ascending();

        //La paginación en JPA es 0-indexada, por ello se resta 1 al numero de pagina recibido.
        Pageable pageable = PageRequest.of(pagina - 1, tamanioPagina, sort);
        Page<User> pageResult = null;
        
        //Filtros
        String nombreBusqueda = nombre;
        String equipoBusqueda = equipoId;
        
        if(nombre != null && nombre.trim().equals("")) {
        	nombre = null; 
        }
        
        if(equipoId != null && equipoId.equals("-1")) {
        	equipoBusqueda = null;
        }
        
        //Busca por nombres y equipo (2 filtros)
    	pageResult = userRepository.buscarUsersCampoPaginado(nombreBusqueda, equipoBusqueda, pageable);
    	
    	long totalRegistros = pageResult.getTotalElements();
    	
    	Page<UserResponse> resultado = pageResult.map(this::toResponse);
        
    	if (resultado.getContent().isEmpty()) {
    		respuesta.setCodigoResultado(UConstante.RESULTADO_NO_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("No se encontraron colaboradores con los criterios especificados.");
    		respuesta.setResultados(List.of());
    		respuesta.setTotalPaginas(0);
    		respuesta.setTotalRegistros(0);
    	} else {
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
    
    
    /**
     * 🚀 Implementación para obtener la lista de colaboradores activos (supervisores).
     * Mapea la lista de entidades a una lista de DTOs simples.
     * @return Lista de ColaboradorSimpleDTO con id, nombres.
     */
    public List<UserResponse> listarPersonasActivasCampo() {
        // Usamos la consulta generada por Spring Data JPA, buscando por estado = 1
        List<User> activos = userRepository.findByEstadoAndTipoTrabajoOrderByNombreAsc(UConstante.ACTIVO, UConstante.TIPO_TRABAJO_CAMPO);

        // Mapea la entidad Colaborador a ColaboradorSimpleDTO
        return activos.stream()
                .map(this::toResponse)
                .toList(); 
    }
    
    /**
     * 🚀 Implementación para obtener la lista de colaboradores activos (supervisores).
     * Mapea la lista de entidades a una lista de DTOs simples.
     * @return Lista de ColaboradorSimpleDTO con id, nombres.
     */
    public List<UserResponse> listarPersonasActivasTotal() {
        // Usamos la consulta generada por Spring Data JPA, buscando por estado = 1
        List<User> activos = userRepository.findByEstadoOrderByNombreAsc(UConstante.ACTIVO);

        // Mapea la entidad Colaborador a ColaboradorSimpleDTO
        return activos.stream()
                .map(this::toResponse)
                .toList(); 
    }
    
    

    /**
     * Obtiene los colaboradores activos de un equipo.
     * @param stridEquipo
     * @return
     */
    public List<UserResponse> getUsuariosEquipo(String stridEquipo) {
        Long idEquipo = Long.parseLong(stridEquipo);

        List<User> usuarios = userRepository.findByEquipoIdAndEstadoAndTipoTrabajoOrderByNombreAsc(idEquipo, UConstante.ACTIVO, UConstante.TIPO_TRABAJO_CAMPO);

        // 🧭 Mapear la entidad Colaborador a un DTO simple
        return usuarios.stream()
                .map(this::toResponse)
                .toList();
    }
    
    
    /**
     * Obtiene los usuarios supervisados por un usuario, de forma jerarquica y recursiva.
     * @param idSupervisor
     * @return
     */
    public List<UserResponse> obtenerSupervisados(Long idSupervisor) {
    	
    	List<User> supervisados = userRepository.findPersonasBySupervisor(idSupervisor);
        
        // 🧭 Mapear la entidad Colaborador a un DTO simple
        return supervisados.stream()
                .map(this::toResponse)
                .toList();
	    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Expone IDs de supervisados para autorización de endpoints de ubicación][obj: UserService.getSupervisadosIds]
    @Transactional(readOnly = true)
    public Set<Long> getSupervisadosIds(Long idSupervisor) {
        return userRepository.findPersonasBySupervisor(idSupervisor).stream()
                .map(User::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }
    
    /**
     * Recupera el usuario en base a su login. 
     * @param usuario
     * @return
     */
    public UserResponse getByUsuario(String usuario) {
        
    	User user = userRepository.findByUsuarioAndEstado(usuario, UConstante.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para usuario=" + usuario));
        
    	log.debug("Usuario encontrado saaSub={}", usuario);
    	
        return toResponse(user);
    }
    
    /**
     * Recupera el usuario en base a su login. 
     * @param usuario
     * @return
     */
    public UserResponse getById(Long id) {
        
    	User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para id=" + id));
        
    	log.debug("Usuario encontrado id={}", id);
    	
        return toResponse(user);
    }
    
    
    @Transactional(readOnly = true)
    public boolean verifyPassword(String usuario, String rawPassword) {
        return userRepository.findPasswordHashByUsuario(usuario)
                .map(hash -> passwordEncoder.matches(rawPassword, hash))
                .orElse(false);
    }

    public UserResponse buscarUsuariosLoginDistintoId(Long id, String usuario) {

        List<User> users = userRepository.findByUsuarioAndIdNot(id, usuario);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado para id=" + id);
        }

        return toResponse(users.get(0)); // O lanzar excepción si hay más de uno
    }
    
}
