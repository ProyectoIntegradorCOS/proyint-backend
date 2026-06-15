package pe.gob.onp.thaqhiri.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.auth.SaaProperties;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.SincronizacionDTO;
import pe.gob.onp.thaqhiri.dto.UserRequest;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.dto.UsuarioPerfilDTO;
import pe.gob.onp.thaqhiri.dto.UsuarioSaaDTO;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import pe.gob.onp.thaqhiri.service.SincronizacionService;
import pe.gob.onp.thaqhiri.service.UserService;
import pe.gob.onp.thaqhiri.service.UsuarioSaaService;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.USesion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestión de usuarios")
@Slf4j
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	private final SaaProperties saaproperties;
    private final UserService userService;
    private final UsuarioSaaService usuarioSaaService;
    private final SincronizacionService sincronizacionService;

    public UserController(UserService userService, UsuarioSaaService usuarioSaaService, SaaProperties saaProperties, SincronizacionService sincronizacionService) {
        this.userService = userService;
        this.usuarioSaaService = usuarioSaaService;
        this.saaproperties = saaProperties;
        this.sincronizacionService = sincronizacionService;
    }

    @PostMapping
    @Operation(
            summary = "Crear/Actualizar usuario",
            description = "Registra o actualiza un usuario por su identidad SAA",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    public ResponseEntity<UserResponse> createOrUpdateBySaaSubject(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload de registro/actualización de usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            ) UserRequest request,
            HttpServletRequest httpRequest) 
    {    	
        
        
        String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:58 UTC-5 (Lima)][desc: Fuerza usuarioSesion desde token][obj: UserController.createOrUpdateBySaaSubject]
        UserRequest payload = new UserRequest(
                request.id(),
                request.saaSubject(),
                request.usuario(),
                request.nombre(),
                request.estado(),
                request.equipoId(),
                request.horarioId(),
                request.email(),
                usuarioSesion
        );
        log.debug("Recibida petición de registro/actualización de usuario: {}", request.saaSubject());
        
        try {
            UserResponse response = userService.createOrUpdateBySaaSubject(payload, usuarioSesion, terminalSesion);
            log.debug("Usuario procesado correctamente: {}", response.saaSubject());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error procesando usuario {}", request.saaSubject(), ex);
            throw ex;
        }
    }
    
    @PostMapping("/by-id")
    @Operation(
            summary = "Crear/Actualizar usuario por Id",
            description = "Registra o actualiza un usuario por su Id de Thaqhiri",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    public ResponseEntity<UserResponse> createOrUpdateById(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload de registro/actualización de usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            ) UserRequest request,
            HttpServletRequest httpRequest) 
    {
        
        String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:58 UTC-5 (Lima)][desc: Fuerza usuarioSesion desde token][obj: UserController.createOrUpdateById]
        UserRequest payload = new UserRequest(
                request.id(),
                request.saaSubject(),
                request.usuario(),
                request.nombre(),
                request.estado(),
                request.equipoId(),
                request.horarioId(),
                request.email(),
                usuarioSesion
        );
        
        try {
            UserResponse response = userService.createOrUpdateById(payload, usuarioSesion, terminalSesion);
            log.info("Usuario procesado correctamente: {} (saaSub almacenado={})", response.saaSubject(), response.saaSubject());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error procesando usuario {}", request.saaSubject(), ex);
            throw ex;
        }
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados")
    public List<UserResponse> findAll() {
        log.debug("Listando usuarios registrados");
        try {
            return userService.findAll();
        } catch (Exception ex) {
            log.error("Error listando usuarios", ex);
            throw ex;
        }
    }



    
    /**
	 * Endpoint para búsqueda por nombres del colaborador.
     * URL ejemplo: GET /api/colaborador?nombres=Miguel&pagina=1&tamanioPagina=10&orden=asc&columnaOrden=nombres
	 * @param nombres Filtro de búsqueda para los nombres, tipo String.
     * @param pagina Número de página, tipo Integer.
     * @param tamanio Tamaño de la página en filas, tipo Integer.
     * @param orden Tipo de ordenamiento, tipo String.
     * @param columnaOrden Nombre de la columna para el ordenamiento, tipo String.	     
	 * @return Devuelve una respuesta, de tipo RespuestaBusquedaDTO<ColaboradorDTO>.
	 */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar usuarios",
            description = "Busca usuarios con filtros y paginación"
    )
    public ResponseEntity<RespuestaBusquedaDTO<UserResponse>> buscar(
    		@RequestParam(required = false) String nombre,
    		@RequestParam(required = false) String equipoId,        		
    		@RequestParam(defaultValue = "1") int pagina,
    		@RequestParam(defaultValue = "10") int tamanioPagina,
    		@RequestParam(defaultValue = "asc") String orden,
    		@RequestParam(defaultValue = "nombre") String columnaOrden) {
    	
    	RespuestaBusquedaDTO<UserResponse> respuesta = userService.buscarUsuariosCampoPaginado(nombre, equipoId, pagina, tamanioPagina, orden, columnaOrden);
    	
    	return ResponseEntity.ok(respuesta);
    }
    
    
    /**
     * 🚀 Endpoint para obtener la lista simple de colaboradores de campo activos.
     * Ruta: GET /api/colaborador/supervisores-activos
     */
    @GetMapping("/personas/campo")
    @Operation(
            summary = "Listar personas de campo",
            description = "Devuelve la lista simple de colaboradores de campo activos"
    )
    public ResponseEntity<RespuestaDTO<?>> listarPersonasActivasCampo() {
        
    	RespuestaDTO<?> response = null;
        List<UserResponse> personas = userService.listarPersonasActivasCampo();
        
        if(personas.size() > 0) {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas obtenida exitosamente.", personas);
        }
        else {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas activas.", personas);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 🚀 Endpoint para obtener la lista simple de colaboradores total activos.
     * Ruta: GET /api/colaborador/supervisores-activos
     */
    @GetMapping("/personas/total")
    @Operation(
            summary = "Listar personas total activas",
            description = "Devuelve la lista simple de colaboradores activos"
    )
    public ResponseEntity<RespuestaDTO<?>> listarPersonasActivasTotal() {
        
    	RespuestaDTO<?> response = null;
        List<UserResponse> personas = userService.listarPersonasActivasTotal();
        
        if(personas.size() > 0) {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas obtenida exitosamente.", personas);
        }
        else {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas activas.", personas);
        }
        
        return ResponseEntity.ok(response);
    }
    
    
    /**
     * 🚀 Endpoint para obtener la lista simple de colaboradores activos.
     * Ruta: GET /api/colaborador/supervisores-activos
     */
    @GetMapping("/personas/equipo")
    @Operation(
            summary = "Listar personas por equipo",
            description = "Obtiene colaboradores activos por equipo"
    )
    public ResponseEntity<RespuestaDTO<?>> getPersonasEquipo(@RequestParam("idEquipo") String idEquipo) {
        
    	RespuestaDTO<?> response = null;
        List<UserResponse> personas = userService.getUsuariosEquipo(idEquipo);
                    
        if(personas.size() > 0) {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas supervisadas obtenida exitosamente.", personas);
        }
        else {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas supervisadas por el usuario indicado.", personas);
        }
        
        return ResponseEntity.ok(response);
    }
    
    
    
    @GetMapping("/supervisados")
    @Operation(
            summary = "Listar supervisados",
            description = "Lista personas supervisadas por un supervisor"
    )
    public ResponseEntity<RespuestaDTO<?>> obtenerSupervisadosJerarquia(@RequestParam("idSupervisor") Long idSupervisor) {
    	
    	RespuestaDTO<?> response = null;
        List<UserResponse> personas = userService.obtenerSupervisados(idSupervisor);
                    
        if(personas.size() > 0) {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas supervisadas obtenida exitosamente.", personas);
        }
        else {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas supervisadas por el usuario indicado.", personas);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/by-colaborador")
    @Operation(
            summary = "Crear/Actualizar colaborador",
            description = "Registra o actualiza un usuario por su Id de Thaqhiri",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    public ResponseEntity<UserResponse> createOrUpdateColaborador(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload de registro/actualización de usuario",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRequest.class))
            ) UserRequest request,
            HttpServletRequest httpRequest) 
    {        
        try {
        	String usuarioSesion = USesion.resolveUsuario();
        	String terminalSesion = USesion.determineHost(httpRequest);
        	
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:58 UTC-5 (Lima)][desc: Fuerza usuarioSesion desde token][obj: UserController.createOrUpdateColaborador]
            UserRequest payload = new UserRequest(
                    request.id(),
                    request.saaSubject(),
                    request.usuario(),
                    request.nombre(),
                    request.estado(),
                    request.equipoId(),
                    request.horarioId(),
                    request.email(),
                    usuarioSesion
            );
            UserResponse response = userService.createOrUpdateById(payload, usuarioSesion, terminalSesion);
            log.info("Usuario procesado correctamente: {} (saaSub almacenado={})", response.saaSubject(), response.saaSubject());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error procesando usuario {}", request.saaSubject(), ex);
            throw ex;
        }
    }
    
    
    /**
     * 🚀 Endpoint para obtener la lista simple de colaboradores activos.
     * Ruta: GET /api/colaborador/supervisores-activos
     */
    @GetMapping("/buscar-usuario-saa")
    @Operation(
            summary = "Buscar usuario SAA",
            description = "Consulta el usuario en SAA por login"
    )
    public ResponseEntity<RespuestaDTO<?>> buscarUsuarioSaa(@RequestParam("usuario") String usuario) {

        String urlBuscarUsuarioSaa = this.saaproperties.getBuscarUsuarioUrl();
        String codigoSistema = this.saaproperties.getSystemCodeThaqhiri();
        
        RespuestaDTO<UsuarioSaaDTO> respuestaSaa = usuarioSaaService.buscarUsuarioSaa(urlBuscarUsuarioSaa, codigoSistema, usuario);

        if (respuestaSaa == null) {
            return ResponseEntity.ok(
                    new RespuestaDTO<>(
                            "" + UConstante.RESULTADO_ERROR,
                            "Error al comunicarse con el servicio de Saa.",
                            null
                    )
            );
        }

        // Caso 1: Encontró usuario (codigoResultado = "00")
        if ("00".equals(respuestaSaa.getCodigoResultado())
                && respuestaSaa.getResultados() != null
                && !respuestaSaa.getResultados().isEmpty()) {

            return ResponseEntity.ok(
                    new RespuestaDTO<>(
                            "" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,
                            respuestaSaa.getMensajeResultado(),
                            respuestaSaa.getResultados()
                    )
            );
        }

        // Caso 2: No encontró usuario (codigoResultado = "01")
        if ("01".equals(respuestaSaa.getCodigoResultado())) {
            return ResponseEntity.ok(
                    new RespuestaDTO<>(
                            "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                            respuestaSaa.getMensajeResultado(),
                            null
                    )
            );
        }

        // Cualquier otro caso inesperado
        return ResponseEntity.ok(
                new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_ERROR,
                        "Respuesta inesperada del servicio de Saa.",
                        null
                )
        );
    }

 

    /**
     * Busca si un usuario activo existe en Thaquiri en base a su login.
     * @param login
     * @return
     */
    @GetMapping("/buscar-login")
    @Operation(
            summary = "Buscar usuario por login",
            description = "Busca un usuario activo por login en Thaqhiri"
    )
    public ResponseEntity<RespuestaDTO<UserResponse>> buscarUsuarioLogin(@RequestParam(required = true) String login) {
    	
    	RespuestaDTO<UserResponse> respuesta = new RespuestaDTO<>();
    	
    	try {
    		if(login == null) {
    			throw new Exception("El login no puede ser nulo.");
    		}
    		
    		UserResponse usuario = userService.getByUsuario(login.trim().toUpperCase());    		
    		
	        respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_SI_ENCONTRO_FILAS));
    		respuesta.setMensajeResultado("Búsqueda exitosa.");
    		respuesta.setResultados(List.of(usuario));
	    	
	    	return ResponseEntity.ok(respuesta);
    	}
    	catch(Exception e) {
    		respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_NO_ENCONTRO_FILAS));
    		respuesta.setMensajeResultado(e.getMessage());
    		respuesta.setResultados(List.of());
    		
    		return ResponseEntity.ok(respuesta);    		
    	}
    }
    
    /**
     * Busca si existe un usuario con el mismo login pero distinto id.
     * Se utiliza para validar duplicidad el editar un colaborador. 
     * @param id
     * @param login
     * @return
     */
    @GetMapping("/buscar-otro-usuario-id-login")
    @Operation(
            summary = "Buscar usuario por login excluyendo id",
            description = "Valida duplicidad de login excluyendo un id específico"
    )
    public ResponseEntity<RespuestaDTO<UserResponse>> buscarOtroUsuarioIdLogin(@RequestParam(required = true) String id, 
    		                                                      @RequestParam(required = true) String login) {
    	
    	RespuestaDTO<UserResponse> respuesta = new RespuestaDTO<>();
    	
    	try {
    		if(id == null) {
    			throw new Exception("El id no puede ser nulo.");
    		}
    		
    		if(login == null) {
    			throw new Exception("El login no puede ser nulo.");
    		}    		
    		
    		//Busca por id y login
			Long idLong = Long.valueOf(id);
			UserResponse usuario = userService.buscarUsuariosLoginDistintoId(idLong, login.trim().toUpperCase());		
    		
	        respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_SI_ENCONTRO_FILAS));
    		respuesta.setMensajeResultado("Búsqueda exitosa.");
    		respuesta.setResultados(List.of(usuario));
	    	
	    	return ResponseEntity.ok(respuesta);
    	}
    	catch(Exception e) {
    		respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_NO_ENCONTRO_FILAS));
    		respuesta.setMensajeResultado(e.getMessage());
    		respuesta.setResultados(List.of());
    		
    		return ResponseEntity.ok(respuesta);    		
    	}
    }
    
    
    
    @GetMapping("/obtener-datos-sincronizar")
    @Operation(
            summary = "Obtener datos de sincronización",
            description = "Recupera datos necesarios para sincronización"
    )
    public ResponseEntity<SincronizacionDTO> obtenerDatosSincronizar(HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	SincronizacionDTO respuesta = new SincronizacionDTO();
    	
    	try {
    		respuesta = sincronizacionService.obtenerDatosSincronizar(usuarioSesion, terminalSesion);	
    		
    		respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_EXITOSO));
			respuesta.setMensajeResultado("Datos de sincronización recuperados.");			
			return ResponseEntity.ok(respuesta);
    	}
    	catch(Exception e) {
    		respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_ERROR));
    		respuesta.setMensajeResultado("Datos de sincronización no recuperados.");    		
    		return ResponseEntity.ok(respuesta);    		
    	}
    }
    
    @PostMapping("/sincronizar")
    @Operation(
            summary = "Sincronizar usuarios SAA",
            description = "Sincroniza usuarios SAA con información local"
    )
    public ResponseEntity<RespuestaDTO<String>> sincronizarUsuariosSaa(@RequestBody List<UsuarioPerfilDTO> listaUsuarios,
    		HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	RespuestaDTO<String> respuesta = new RespuestaDTO<>();
    	
    	try {
	    	sincronizacionService.sincronizar(listaUsuarios, usuarioSesion, terminalSesion);
	        
	    	respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_EXITOSO));
			respuesta.setMensajeResultado("Sincronización exitosa.");
			respuesta.setResultados(List.of());
			
			return ResponseEntity.ok(respuesta);
    	}
    	catch(Exception e) {
    		respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_ERROR));
    		respuesta.setMensajeResultado("Sincronización no exitosa.");
    		respuesta.setResultados(List.of());
    		
    		return ResponseEntity.ok(respuesta);    		
    	}
    }    
    

    @GetMapping("/{saaSubject}")
    @Operation(summary = "Obtener usuario", description = "Busca un usuario por su identidad SAA")
    public UserResponse getBySaaSubject(
            @Parameter(description = "Identificador SAA (claim sub)", required = true)
            @PathVariable String saaSubject) {
    	
    	
        try {
            UserResponse user = userService.getBySaaSubject(saaSubject);
            log.info("Usuario encontrado para saaSubject={} -> {}", saaSubject, user);
            return user;
        } catch (ResourceNotFoundException ex) {
            log.info("Usuario no registrado aún para saaSubject={}", saaSubject);
            throw ex;
        } catch (Exception ex) {
            log.error("Error consultando usuario {}", saaSubject, ex);
            throw ex;
        }
    }
    
}
