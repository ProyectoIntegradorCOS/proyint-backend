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
import pe.gob.onp.thaqhiri.auth.SaaAuthenticationFilter;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.dto.DailyDistanceResponse;
import pe.gob.onp.thaqhiri.dto.LocationBatchRequest;
import pe.gob.onp.thaqhiri.dto.LocationCreateRequest;
import pe.gob.onp.thaqhiri.dto.LocationHistoryResponse;
import pe.gob.onp.thaqhiri.dto.LocationResponse;
import pe.gob.onp.thaqhiri.service.LocationService;
import pe.gob.onp.thaqhiri.service.UbicacionesService;
import pe.gob.onp.thaqhiri.service.UserService;
import pe.gob.onp.thaqhiri.util.USesion;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "Endpoints de ubicaciones: creación, historial y distancia diaria")
@Slf4j
public class LocationController {

	private static final Logger log = LoggerFactory.getLogger(LocationController.class);
    private final LocationService locationService;
    private final UbicacionesService ubicacionService;
    private final UserService userService;
    private final MeterRegistry meterRegistry;

    public LocationController(
            LocationService locationService,
            UbicacionesService ubicacionService,
            UserService userService,
            MeterRegistry meterRegistry
    ) {
        this.locationService = locationService;
        this.ubicacionService = ubicacionService;
        this.userService = userService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping
    @Operation(
            summary = "Registrar ubicación",
            description = "Crea un registro de ubicación para un usuario identificado por su identidad SAA",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = LocationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    public ResponseEntity<LocationResponse> create(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload de creación de ubicación",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LocationCreateRequest.class))
            ) LocationCreateRequest request,
            HttpServletRequest httpRequest) {
        
    	long start = System.nanoTime();
        String status = "success";
        
        String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
        
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Traza de recepción de ubicación (correlación por X-Trace-Id)][obj: LocationController.create]
        log.info("TRACE {} /api/locations received saaSub={} ts={}",
                traceId != null ? traceId : "-", request.saaSubject(), request.timestamp());
        log.debug("Recibida ubicación para saaSub={} lat={} lng={}",
                request.saaSubject(), request.latitude(), request.longitude());
        try {
            LocationResponse response = locationService.create(request, usuarioSesion, terminalSesion);
            log.debug("Ubicación almacenada con id={}", response.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.error("Error almacenando ubicación para saaSub={}", request.saaSubject(), ex);
            status = "error";
            throw ex;
        } finally {
            recordLocationMetric("single", status, System.nanoTime() - start);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Restaura endpoint batch consumido por la app (/api/locations/batch)][obj: LocationController.createBatch]
    @PostMapping("/batch")
    @Operation(
            summary = "Registrar ubicaciones por lote",
            description = "Crea múltiples registros de ubicación en una sola petición",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Creado",
                            content = @Content(schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
            }
    )
    public ResponseEntity<Integer> createBatch(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payload de creación de ubicaciones (batch)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LocationBatchRequest.class))
            ) LocationBatchRequest request,
            HttpServletRequest httpRequest
    ) {
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        long start = System.nanoTime();
        String status = "success";
        
        try {
            int count = locationService.createBatch(request, usuarioSesion, terminalSesion);

            return ResponseEntity.status(HttpStatus.CREATED).body(count);
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordLocationMetric("batch", status, System.nanoTime() - start);
        }
    }

    @GetMapping("/history")
    @Operation(
            summary = "Historial de ubicaciones",
            description = "Obtiene el historial de ubicaciones entre un rango de fechas (UTC)",
            responses = @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = LocationHistoryResponse.class)))
    )
    public LocationHistoryResponse history(
            @Parameter(description = "Identificador SAA (claim sub) del usuario", required = true)
            @RequestParam String saaSubject,
            @Parameter(description = "Fecha/hora de inicio (UTC)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @Parameter(description = "Fecha/hora de fin (UTC)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end
    ) {
        
        log.debug("Consultando historial para uid={} start={} end={}", saaSubject, start, end);
        try {
            return locationService.getHistory(saaSubject, start, end);
        } catch (Exception ex) {
            log.error("Error consultando historial para uid={}", saaSubject, ex);
            throw ex;
        }
    }

    @GetMapping("/distance")
    @Operation(
            summary = "Distancia diaria",
            description = "Obtiene la distancia total recorrida por día (UTC)",
            responses = @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = DailyDistanceResponse.class)))
    )
    public DailyDistanceResponse distance(
            @Parameter(description = "Identificador SAA (claim sub) del usuario", required = true)
            @RequestParam String saaSubject,
            @Parameter(description = "Fecha (UTC) en formato ISO yyyy-MM-dd")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        
        log.debug("Consultando distancia para uid={} fecha={}", saaSubject, date);
        
        try {
            return locationService.getDailyDistance(saaSubject, date);
        } catch (Exception ex) {
            log.error("Error consultando distancia para uid={} fecha={} ", saaSubject, date, ex);
            throw ex;
        }
    }


    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Registra métricas dedicadas de ubicaciones][obj: LocationController.recordLocationMetric]
    private void recordLocationMetric(String type, String status, long durationNs) {
        Tags tags = Tags.of("type", type, "status", status);
        meterRegistry.counter("thaqhiri_backend_locations_total", tags).increment();
        Timer.builder("thaqhiri_backend_locations_duration")
                .tags(tags)
                .register(meterRegistry)
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        if ("error".equalsIgnoreCase(status)) {
            meterRegistry.counter("thaqhiri_backend_locations_errors_total", Tags.of("type", type)).increment();
        }
    }
    
    
    
    
    /**
     * Nuevo endpoint: devuelve los datos de una fecha y también el timestamp de la consulta.
     * Ejemplo:
     * GET /api/geoespacial/inicial?persona=101,245&fecha=2025-10-23
     */
    @GetMapping(value = "/buscar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> buscarUbicacionesConTimestamp(
            @RequestParam("persona") String idPersona,
            @RequestParam("fecha") String fechaIso,
            @RequestParam(value = "timestamp", required = false) String timestamp) {

        try {
            if (idPersona == null || idPersona.isBlank()) {
        	    return ResponseEntity.badRequest().body("{\"error\":\"Debe especificar al menos un ID de persona\"}");
        	}
            
            String geojson = ubicacionService.getGeoJsonByPersonaAndDateWithTimestamp(idPersona, fechaIso, timestamp);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.ok().headers(headers).body(geojson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
}
