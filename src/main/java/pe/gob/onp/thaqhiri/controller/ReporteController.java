package pe.gob.onp.thaqhiri.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.gob.onp.thaqhiri.dto.ReporteProductividadDTO;
import pe.gob.onp.thaqhiri.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: ReporteController swagger]
@Tag(name = "Reportes", description = "Reportes de productividad y seguimiento")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;

    @Operation(summary = "Reporte de productividad", description = "Obtiene productividad por equipo, persona y fecha")
    @GetMapping("/productividad")
    public ResponseEntity<List<ReporteProductividadDTO>> obtenerReporte(
            @RequestParam(required = false) String idPersona,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return ResponseEntity.ok(
                service.obtenerReporte(idPersona, fechaInicio, fechaFin)
        );
    }
    
}
