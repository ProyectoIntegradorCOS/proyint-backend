package pe.gob.onp.thaqhiri.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.dto.DestinoImportResultDTO;
import pe.gob.onp.thaqhiri.dto.DestinoImportRowDTO;
import pe.gob.onp.thaqhiri.dto.DestinoDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.model.ResultadoValidacion;
import pe.gob.onp.thaqhiri.model.TipoResultadoValidacion;
import pe.gob.onp.thaqhiri.service.DestinoImportService;
import pe.gob.onp.thaqhiri.service.DestinoService;
import pe.gob.onp.thaqhiri.service.MapboxGeocodingService;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.USesion;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: DestinoController swagger]
@Tag(name = "Destinos", description = "Gestion de destinos y catalogo")
@RestController
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoService destinoService;
    private final DestinoImportService destinoImportService;
    private final MapboxGeocodingService mapboxGeocodingService;
    private final MeterRegistry meterRegistry;

    public DestinoController(
            DestinoService destinoService,
            DestinoImportService destinoImportService,
            MapboxGeocodingService mapboxGeocodingService,
            MeterRegistry meterRegistry
    ) {
        this.destinoService = destinoService;
        this.destinoImportService = destinoImportService;
        this.mapboxGeocodingService = mapboxGeocodingService;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Buscar destinos", description = "Consulta destinos con filtros y paginacion")
    @GetMapping("/buscar")
    public ResponseEntity<RespuestaBusquedaDTO<DestinoDTO>> buscar(
            @RequestParam(required = false) String destino,
            @RequestParam(required = false) String direccion,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanioPagina,
            @RequestParam(defaultValue = "asc") String orden,
            @RequestParam(defaultValue = "nombre") String columnaOrden,
            @RequestParam(defaultValue = "fechaActualizacionOnp") String columnaSegundoOrden
    ) {
    	RespuestaBusquedaDTO<DestinoDTO> respuesta = destinoService.buscarPaginado(destino, direccion, pagina, tamanioPagina, orden, columnaOrden, columnaSegundoOrden);

        return ResponseEntity.ok(respuesta);
    }

    @Operation(summary = "Registrar destino", description = "Crea un destino con datos de auditoria")
    @PostMapping("/registrar")
    public ResponseEntity<RespuestaDTO<DestinoDTO>> registrar(@RequestBody DestinoDTO dto, HttpServletRequest httpRequest) {

    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	DestinoDTO creado = destinoService.registrar(dto, usuarioSesion, terminalSesion);
        
        return ResponseEntity.ok(
                new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_EXITOSO,
                        "Destino registrado correctamente.",
                        List.of(creado)
                )
        );
    }

    @Operation(summary = "Actualizar destino", description = "Actualiza un destino existente")
    @PutMapping("/actualizar")
    public ResponseEntity<RespuestaDTO<DestinoDTO>> actualizar(@RequestBody DestinoDTO dto, HttpServletRequest httpRequest) {
        
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
       
        DestinoDTO actualizado = destinoService.actualizar(dto, usuarioSesion, terminalSesion);
        
        return ResponseEntity.ok(
                new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_EXITOSO,
                        "Destino actualizado correctamente.",
                        List.of(actualizado)
                )
        );
    }

    @Operation(summary = "Eliminar destino", description = "Eliminacion logica de destino por id")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDTO<DestinoDTO>> eliminar(@PathVariable Long id, HttpServletRequest httpRequest) {
        
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);    	
        
        String msg = destinoService.eliminar(id, usuarioSesion, terminalSesion);
        if (msg == null) {
            return ResponseEntity.ok(new RespuestaDTO<>("0", "Destino eliminado correctamente.", List.of()));
        }
        return ResponseEntity.ok(new RespuestaDTO<>("1", msg, List.of()));
    }

    

        
    
    @Operation(summary = "Descargar plantilla", description = "Descarga plantilla Excel de destinos")
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        long start = System.nanoTime();
        String status = "success";

        try {
            byte[] excel = destinoService.generarPlantillaDestinos();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla-destinos.xlsx\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);

        } catch (Exception ex) {
            status = "error";
            return ResponseEntity.internalServerError().build();
        } finally {
            recordDestinoMetric("destino_template", status, System.nanoTime() - start);
        }
    }


    @Operation(summary = "Geocodificar destino", description = "Busca coordenadas con Mapbox")
    @GetMapping("/geocode")
    public ResponseEntity<RespuestaDTO<MapboxSuggestionDTO>> geocode(
            @RequestParam String query
    ) {
        if (!mapboxGeocodingService.isConfigured()) {
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                    "Mapbox no está configurado (defina MAPBOX_ACCESS_TOKEN en el backend).",
                    List.of()
            ));
        }
        try {
            var sug = mapboxGeocodingService.forwardGeocode(query);
            if (sug.isEmpty()) {
                return ResponseEntity.ok(new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                        "Sin resultados",
                        List.of()
                ));
            }
            var s = sug.get();
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,
                    "OK",
                    List.of(new MapboxSuggestionDTO(s.placeId(), s.label(), s.lat(), s.lng()))
            ));
        } catch (MapboxGeocodingService.MapboxException ex) {
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                    ex.getMessage(),
                    List.of()
            ));
        }
    }

    @Operation(summary = "Reverse geocode", description = "Convierte coordenadas a direccion")
    @GetMapping("/reverse-geocode")
    public ResponseEntity<RespuestaDTO<MapboxReverseDTO>> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        if (!mapboxGeocodingService.isConfigured()) {
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                    "Mapbox no está configurado (defina MAPBOX_ACCESS_TOKEN en el backend).",
                    List.of()
            ));
        }
        try {
            var sug = mapboxGeocodingService.reverseGeocode(lat, lng);
            if (sug.isEmpty()) {
                return ResponseEntity.ok(new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                        "Sin resultados",
                        List.of()
                ));
            }
            var s = sug.get();
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,
                    "OK",
                    List.of(new MapboxReverseDTO(
                            s.placeId(),
                            s.label(),
                            s.direccion(),
                            s.departamento(),
                            s.provincia(),
                            s.distrito(),
                            lat,
                            lng
                    ))
            ));
        } catch (MapboxGeocodingService.MapboxException ex) {
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,
                    ex.getMessage(),
                    List.of()
            ));
        }
    }


    public record MapboxSuggestionDTO(String placeId, String label, double lat, double lng) {}

    public record MapboxReverseDTO(
            String placeId,
            String label,
            String direccion,
            String departamento,
            String provincia,
            String distrito,
            double lat,
            double lng
    ) {}
    
    
    
    @Operation(summary = "Obtener destino", description = "Busca un destino por id")
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<DestinoDTO>> obtenerPorId(@PathVariable Long id) {

        DestinoDTO destino = destinoService.obtenerPorId(id);

        if (destino == null) {
            return ResponseEntity.ok(
                    new RespuestaDTO<>(
                            "" + UConstante.RESULTADO_ERROR,
                            "No se encontró el destino.",
                            List.of()
                    )
            );
        }

        return ResponseEntity.ok(
                new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_EXITOSO,
                        "OK",
                        List.of(destino)
                )
        );
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Registra métricas de import/export de destinos][obj: DestinoController.recordDestinoMetric]
    private void recordDestinoMetric(String action, String status, long durationNs) {
        Tags tags = Tags.of("action", action, "status", status);
        meterRegistry.counter("thaqhiri_backend_destino_ops_total", tags).increment();
        Timer.builder("thaqhiri_backend_destino_ops_duration")
                .tags(tags)
                .register(meterRegistry)
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        if ("error".equalsIgnoreCase(status)) {
            meterRegistry.counter("thaqhiri_backend_destino_ops_errors_total", Tags.of("action", action)).increment();
        }
    }
    
}
