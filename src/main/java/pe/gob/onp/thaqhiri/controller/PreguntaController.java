package pe.gob.onp.thaqhiri.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import pe.gob.onp.thaqhiri.dto.PreguntaDTO;
import pe.gob.onp.thaqhiri.entity.Pregunta;
import pe.gob.onp.thaqhiri.service.PreguntaService;
import pe.gob.onp.thaqhiri.util.USesion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: PreguntaController swagger]
@Tag(name = "Preguntas", description = "Gestion de preguntas de cuestionarios")
@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService service;

    // Buscar con paginación
    @Operation(summary = "Listar preguntas", description = "Lista preguntas por cuestionario")
    @GetMapping("/cuestionario/{idCuestionario}")
    public List<PreguntaDTO> listarPorCuestionario(@PathVariable long idCuestionario) {
        return service.listarPorCuestionario(idCuestionario);
    }

    // Crear
    @Operation(summary = "Crear pregunta", description = "Registra una pregunta en un cuestionario")
    @PostMapping
    public PreguntaDTO crear(@RequestBody PreguntaDTO dto, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return service.crear(dto, usuarioSesion, terminalSesion);
    }

    // Actualizar
    @Operation(summary = "Actualizar pregunta", description = "Actualiza una pregunta existente")
    @PutMapping("/{id}")
    public PreguntaDTO actualizar(@PathVariable Long id, @RequestBody PreguntaDTO dto, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return service.actualizar(id, dto, usuarioSesion, terminalSesion);
    }

    // Eliminar
    @Operation(summary = "Eliminar pregunta", description = "Eliminacion logica de pregunta por id")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        service.eliminar(id, usuarioSesion, terminalSesion);
    }
    
    @Operation(summary = "Actualizar orden", description = "Actualiza el orden de preguntas")
    @PutMapping("/orden")
    public void actualizarOrden(@RequestBody List<PreguntaDTO> preguntas, HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        service.actualizarOrden(preguntas, usuarioSesion, terminalSesion);
    }
    
}
