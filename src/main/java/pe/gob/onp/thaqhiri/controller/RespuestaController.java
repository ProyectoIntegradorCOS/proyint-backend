package pe.gob.onp.thaqhiri.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import pe.gob.onp.thaqhiri.dto.RespuestaPreguntaDTO;
import pe.gob.onp.thaqhiri.service.RespuestaService;
import pe.gob.onp.thaqhiri.util.USesion;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: RespuestaController swagger]
@Tag(name = "Respuestas", description = "Gestion de respuestas de cuestionarios")
@RestController
@RequestMapping("/api/respuestas")
public class RespuestaController {

    @Autowired
    private RespuestaService service;
    @Autowired
    private MeterRegistry meterRegistry;

    // Listar con paginación
    @Operation(summary = "Obtener respuesta", description = "Busca respuesta por id de pregunta")
    @GetMapping
    public RespuestaPreguntaDTO obtener(@RequestParam(required = true) long idPregunta){
    	
        return service.buscar(idPregunta);
    }

    // Crear
    @Operation(summary = "Crear respuesta", description = "Registra una respuesta de cuestionario")
    @PostMapping
    public RespuestaPreguntaDTO crear(@RequestBody RespuestaPreguntaDTO dto, HttpServletRequest httpRequest){
    	
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
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:27 UTC-5 (Lima)][desc: Registra métricas de operaciones de respuestas][obj: RespuestaController.crear]
            recordRespuestaMetric("respuesta_crear", status, System.nanoTime() - start);
        }
    }

    // Actualizar
    @Operation(summary = "Actualizar respuesta", description = "Actualiza una respuesta existente")
    @PutMapping("/{id}")
    public RespuestaPreguntaDTO actualizar(@PathVariable Long id, @RequestBody RespuestaPreguntaDTO dto, HttpServletRequest httpRequest){
    	
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
            recordRespuestaMetric("respuesta_actualizar", status, System.nanoTime() - start);
        }
    }

    // Eliminar
    @Operation(summary = "Eliminar respuesta", description = "Eliminacion logica de respuesta por id")
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
            recordRespuestaMetric("respuesta_eliminar", status, System.nanoTime() - start);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:42 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: RespuestaController obtener por cuestionario/persona]
    @Operation(summary = "Respuestas por cuestionario", description = "Lista respuestas de un cuestionario por persona")
    @GetMapping("/cuestionario/{idCuestionario}")
    public List<RespuestaPreguntaDTO> obtenerPorCuestionario(
            @PathVariable Long idCuestionario,
            @RequestParam(required = true) Long idPersona
    ) {
        return service.obtenerRespuestasPorCuestionario(idCuestionario, idPersona);
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Expone respuestas por item de visita para mostrar solo lo atendido][obj: RespuestaController obtener por item]
    @Operation(summary = "Respuestas por visita", description = "Lista respuestas filtradas por item de visita")
    @GetMapping("/visit-item/{idItem}")
    public List<RespuestaPreguntaDTO> obtenerPorItem(@PathVariable Long idItem) {
        return service.obtenerRespuestasPorItem(idItem);
    }

    private void recordRespuestaMetric(String action, String status, long durationNs) {
        Tags tags = Tags.of("action", action, "status", status);
        meterRegistry.counter("thaqhiri_backend_respuesta_ops_total", tags).increment();
        Timer.builder("thaqhiri_backend_respuesta_ops_duration")
                .tags(tags)
                .register(meterRegistry)
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        if ("error".equalsIgnoreCase(status)) {
            meterRegistry.counter("thaqhiri_backend_respuesta_ops_errors_total", Tags.of("action", action)).increment();
        }
    }
}
