package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.dto.HorarioDTO;
import pe.gob.onp.thaqhiri.service.HorarioService;
import pe.gob.onp.thaqhiri.util.USesion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: HorarioController swagger]
@Tag(name = "Horarios", description = "Gestion de horarios")
@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService service;

    public HorarioController(HorarioService service) {
        this.service = service;
    }

    @Operation(summary = "Listar horarios", description = "Devuelve todos los horarios")
    @GetMapping
    public ResponseEntity<List<HorarioDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Obtener horario", description = "Busca un horario por id")
    @GetMapping("/{id}")
    public ResponseEntity<HorarioDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear horario", description = "Registra un horario")
    @PostMapping
    public ResponseEntity<HorarioDTO> create(@RequestBody HorarioDTO dto, HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        HorarioDTO created = service.create(dto, usuarioSesion, terminalSesion);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Actualizar horario", description = "Actualiza un horario existente")
    @PutMapping("/{id}")
    public ResponseEntity<HorarioDTO> update(@PathVariable Long id, @RequestBody HorarioDTO dto, HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return ResponseEntity.ok(service.update(id, dto, usuarioSesion, terminalSesion));
    }

}
