package pe.gob.onp.thaqhiri.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import pe.gob.onp.thaqhiri.dto.OpcionDTO;
import pe.gob.onp.thaqhiri.entity.Opcion;
import pe.gob.onp.thaqhiri.service.OpcionService;
import pe.gob.onp.thaqhiri.util.USesion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: OpcionController swagger]
@Tag(name = "Opciones", description = "Gestion de opciones de preguntas")
@RestController
@RequestMapping("/api/opciones")
public class OpcionController {

    @Autowired
    private OpcionService service;

    // Listar con paginación
    @Operation(summary = "Listar opciones", description = "Lista opciones por pregunta")
    @GetMapping
    public List<OpcionDTO> listarPorPregunta(@RequestParam(required = true) long idPregunta) {
    	
        return service.listarPorPregunta(idPregunta);
    }

    // Crear
    @Operation(summary = "Crear opcion", description = "Registra una opcion de pregunta")
    @PostMapping
    public OpcionDTO crear(@RequestBody OpcionDTO dto, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return service.crear(dto, usuarioSesion, terminalSesion);
    }

    // Actualizar
    @Operation(summary = "Actualizar opcion", description = "Actualiza una opcion existente")
    @PutMapping("/{id}")
    public OpcionDTO actualizar(@PathVariable Long id, @RequestBody OpcionDTO dto, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return service.actualizar(id, dto, usuarioSesion, terminalSesion);
    }

    // Eliminar
    @Operation(summary = "Eliminar opcion", description = "Eliminacion logica de opcion por id")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id, HttpServletRequest httpRequest){
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        service.eliminar(id, usuarioSesion, terminalSesion);
    }
}
