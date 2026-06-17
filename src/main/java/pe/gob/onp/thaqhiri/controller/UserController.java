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
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.UserRequest;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import pe.gob.onp.thaqhiri.service.UserService;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.USesion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestión de usuarios")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
            HttpServletRequest httpRequest) {
        String usuarioSesion = USesion.resolveUsuario();
        String terminalSesion = USesion.determineHost(httpRequest);
        UserRequest payload = new UserRequest(
                request.id(), request.saaSubject(), request.usuario(), request.nombre(),
                request.estado(), request.equipoId(), request.horarioId(), request.email(),
                usuarioSesion);
        log.debug("Recibida petición de registro/actualización de usuario: {}", request.saaSubject());
        try {
            UserResponse response = userService.createOrUpdateBySaaSubject(payload, usuarioSesion, terminalSesion);
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
            HttpServletRequest httpRequest) {
        String usuarioSesion = USesion.resolveUsuario();
        String terminalSesion = USesion.determineHost(httpRequest);
        UserRequest payload = new UserRequest(
                request.id(), request.saaSubject(), request.usuario(), request.nombre(),
                request.estado(), request.equipoId(), request.horarioId(), request.email(),
                usuarioSesion);
        try {
            UserResponse response = userService.createOrUpdateById(payload, usuarioSesion, terminalSesion);
            log.info("Usuario procesado correctamente: {}", response.saaSubject());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error procesando usuario {}", request.saaSubject(), ex);
            throw ex;
        }
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios con filtros y paginación")
    public ResponseEntity<RespuestaBusquedaDTO<UserResponse>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String equipoId,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanioPagina,
            @RequestParam(defaultValue = "asc") String orden,
            @RequestParam(defaultValue = "nombre") String columnaOrden) {
        RespuestaBusquedaDTO<UserResponse> respuesta = userService.buscarUsuariosCampoPaginado(
                nombre, equipoId, pagina, tamanioPagina, orden, columnaOrden);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/personas/campo")
    @Operation(summary = "Listar personas de campo", description = "Devuelve colaboradores de campo activos")
    public ResponseEntity<RespuestaDTO<?>> listarPersonasActivasCampo() {
        List<UserResponse> personas = userService.listarPersonasActivasCampo();
        RespuestaDTO<?> response = personas.isEmpty()
                ? new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas activas.", personas)
                : new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas obtenida exitosamente.", personas);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/personas/total")
    @Operation(summary = "Listar personas total activas", description = "Devuelve todos los colaboradores activos")
    public ResponseEntity<RespuestaDTO<?>> listarPersonasActivasTotal() {
        List<UserResponse> personas = userService.listarPersonasActivasTotal();
        RespuestaDTO<?> response = personas.isEmpty()
                ? new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas activas.", personas)
                : new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas obtenida exitosamente.", personas);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/personas/equipo")
    @Operation(summary = "Listar personas por equipo", description = "Obtiene colaboradores activos por equipo")
    public ResponseEntity<RespuestaDTO<?>> getPersonasEquipo(@RequestParam("idEquipo") String idEquipo) {
        List<UserResponse> personas = userService.getUsuariosEquipo(idEquipo);
        RespuestaDTO<?> response = personas.isEmpty()
                ? new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas supervisadas.", personas)
                : new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas supervisadas obtenida exitosamente.", personas);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supervisados")
    @Operation(summary = "Listar supervisados", description = "Lista personas supervisadas por un supervisor")
    public ResponseEntity<RespuestaDTO<?>> obtenerSupervisadosJerarquia(@RequestParam("idSupervisor") Long idSupervisor) {
        List<UserResponse> personas = userService.obtenerSupervisados(idSupervisor);
        RespuestaDTO<?> response = personas.isEmpty()
                ? new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron personas supervisadas.", personas)
                : new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de personas supervisadas obtenida exitosamente.", personas);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/by-colaborador")
    @Operation(summary = "Crear/Actualizar colaborador", description = "Registra o actualiza un usuario por su Id de Thaqhiri",
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
            HttpServletRequest httpRequest) {
        try {
            String usuarioSesion = USesion.resolveUsuario();
            String terminalSesion = USesion.determineHost(httpRequest);
            UserRequest payload = new UserRequest(
                    request.id(), request.saaSubject(), request.usuario(), request.nombre(),
                    request.estado(), request.equipoId(), request.horarioId(), request.email(),
                    usuarioSesion);
            UserResponse response = userService.createOrUpdateById(payload, usuarioSesion, terminalSesion);
            log.info("Usuario procesado correctamente: {}", response.saaSubject());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error procesando usuario {}", request.saaSubject(), ex);
            throw ex;
        }
    }

    @GetMapping("/buscar-login")
    @Operation(summary = "Buscar usuario por login", description = "Busca un usuario activo por login en Thaqhiri")
    public ResponseEntity<RespuestaDTO<UserResponse>> buscarUsuarioLogin(@RequestParam(required = true) String login) {
        RespuestaDTO<UserResponse> respuesta = new RespuestaDTO<>();
        try {
            if (login == null) throw new Exception("El login no puede ser nulo.");
            UserResponse usuario = userService.getByUsuario(login.trim().toUpperCase());
            respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_SI_ENCONTRO_FILAS));
            respuesta.setMensajeResultado("Búsqueda exitosa.");
            respuesta.setResultados(List.of(usuario));
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_NO_ENCONTRO_FILAS));
            respuesta.setMensajeResultado(e.getMessage());
            respuesta.setResultados(List.of());
            return ResponseEntity.ok(respuesta);
        }
    }

    @GetMapping("/buscar-otro-usuario-id-login")
    @Operation(summary = "Buscar usuario por login excluyendo id", description = "Valida duplicidad de login excluyendo un id específico")
    public ResponseEntity<RespuestaDTO<UserResponse>> buscarOtroUsuarioIdLogin(
            @RequestParam(required = true) String id,
            @RequestParam(required = true) String login) {
        RespuestaDTO<UserResponse> respuesta = new RespuestaDTO<>();
        try {
            if (id == null) throw new Exception("El id no puede ser nulo.");
            if (login == null) throw new Exception("El login no puede ser nulo.");
            Long idLong = Long.valueOf(id);
            UserResponse usuario = userService.buscarUsuariosLoginDistintoId(idLong, login.trim().toUpperCase());
            respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_SI_ENCONTRO_FILAS));
            respuesta.setMensajeResultado("Búsqueda exitosa.");
            respuesta.setResultados(List.of(usuario));
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.setCodigoResultado(String.valueOf(UConstante.RESULTADO_NO_ENCONTRO_FILAS));
            respuesta.setMensajeResultado(e.getMessage());
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
            log.info("Usuario encontrado para saaSubject={}", saaSubject);
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
