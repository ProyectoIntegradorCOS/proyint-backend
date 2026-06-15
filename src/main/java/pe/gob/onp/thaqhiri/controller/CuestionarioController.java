package pe.gob.onp.thaqhiri.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.dto.CuestionarioDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.service.CuestionarioService;
import pe.gob.onp.thaqhiri.service.UserService;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.USesion;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioController swagger]
@Tag(name = "Cuestionarios", description = "Gestion de cuestionarios y asignacion por equipo")
@RestController
@RequestMapping("/api/cuestionarios")
public class CuestionarioController {

    @Autowired
    private CuestionarioService service;
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:25 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioController user service]
    @Autowired
    private UserService userService;
    @Autowired
    private MeterRegistry meterRegistry;

    // Listar con paginación
    @Operation(summary = "Buscar cuestionarios", description = "Lista cuestionarios activos con filtro por nombre y paginacion")
    @GetMapping
    public Page<CuestionarioDTO> buscar(@RequestParam(required = false) String nombre,
    		                            @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return service.buscar(nombre, page, size);
    }

    // Crear
    @Operation(summary = "Crear cuestionario", description = "Registra un cuestionario y devuelve su detalle")
    @PostMapping
    public CuestionarioDTO crear(@RequestBody CuestionarioDTO dto, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	long start = System.nanoTime();
        String status = "success";
        
        try {
            return service.crear(dto, usuarioSesion, terminalSesion);
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:27 UTC-5 (Lima)][desc: Registra métricas de operaciones de cuestionarios][obj: CuestionarioController.crear]
            recordCuestionarioMetric("cuestionario_crear", status, System.nanoTime() - start);
        }
    }

    // Actualizar
    @Operation(summary = "Actualizar cuestionario", description = "Actualiza la informacion del cuestionario por id")
    @PutMapping("/{id}")
    public CuestionarioDTO actualizar(@PathVariable Long id, @RequestBody CuestionarioDTO dto, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	long start = System.nanoTime();
        String status = "success";
        try {
            return service.actualizar(id, dto, usuarioSesion, terminalSesion);
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordCuestionarioMetric("cuestionario_actualizar", status, System.nanoTime() - start);
        }
    }

    // Eliminar
    @Operation(summary = "Eliminar cuestionario", description = "Realiza eliminacion logica del cuestionario")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	long start = System.nanoTime();
        String status = "success";
        
        try {
            service.eliminar(id, usuarioSesion, terminalSesion);
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordCuestionarioMetric("cuestionario_eliminar", status, System.nanoTime() - start);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:25 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioController.obtenerActivo]
    @Operation(summary = "Obtener cuestionario activo", description = "Obtiene el cuestionario activo del equipo del usuario autenticado")
    @GetMapping("/activo")
    public ResponseEntity<CuestionarioDTO> obtenerActivo(@AuthenticationPrincipal SaaPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var user = userService.getEntityBySaaSubject(principal.getName());
        if (user.getEquipo() == null || user.getEquipo().getId() == null) {
            return ResponseEntity.noContent().build();
        }
        var optional = service.obtenerActivoPorEquipo(user.getEquipo().getId().longValue());
        if (optional.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(optional.get());
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:42 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioController.obtenerActivoPorEquipo]
    @Operation(summary = "Obtener cuestionario activo por equipo", description = "Busca el cuestionario activo del equipo indicado")
    @GetMapping("/activo-equipo")
    public ResponseEntity<CuestionarioDTO> obtenerActivoPorEquipo(
            @RequestParam(required = true) Long idEquipo
    ) {
        if (idEquipo == null) {
            return ResponseEntity.noContent().build();
        }
        var optional = service.obtenerActivoPorEquipo(idEquipo);
        if (optional.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(optional.get());
    }

    private void recordCuestionarioMetric(String action, String status, long durationNs) {
        Tags tags = Tags.of("action", action, "status", status);
        meterRegistry.counter("thaqhiri_backend_cuestionario_ops_total", tags).increment();
        Timer.builder("thaqhiri_backend_cuestionario_ops_duration")
                .tags(tags)
                .register(meterRegistry)
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        if ("error".equalsIgnoreCase(status)) {
            meterRegistry.counter("thaqhiri_backend_cuestionario_ops_errors_total", Tags.of("action", action)).increment();
        }
    }
    
    @GetMapping("/listar")
    public ResponseEntity<RespuestaDTO<?>> listarCuestionarios() {
        
    	RespuestaDTO<?> response = null;
        List<CuestionarioDTO> personas = service.listarCuestionarios();
        
        if(personas.size() > 0) {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS, "Lista de cuestionarios obtenida exitosamente.", personas);
        }
        else {
        	response = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS, "No se encontraron cuestionarios activos.", personas);
        }
        
        return ResponseEntity.ok(response);
    }
}
