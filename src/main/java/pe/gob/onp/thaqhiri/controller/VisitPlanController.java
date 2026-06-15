package pe.gob.onp.thaqhiri.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;
import pe.gob.onp.thaqhiri.dto.EquipoDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.dto.VisitItemCompleteRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemCreateRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemPendingReprogramResponse;
import pe.gob.onp.thaqhiri.dto.VisitItemReassignRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemResponse;
import pe.gob.onp.thaqhiri.dto.VisitItemStateChangeRequest;
import pe.gob.onp.thaqhiri.dto.VisitItemsReorderRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanImportResultDTO;
import pe.gob.onp.thaqhiri.dto.VisitPlanRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanResponse;
import pe.gob.onp.thaqhiri.dto.VisitPlanValidaResultDTO;
import pe.gob.onp.thaqhiri.dto.VisitaPlanMasivoRequest;
import pe.gob.onp.thaqhiri.model.ResultadoValidacion;
import pe.gob.onp.thaqhiri.model.TipoResultadoValidacion;
import pe.gob.onp.thaqhiri.service.VisitPlanImportService;
import pe.gob.onp.thaqhiri.service.VisitPlanService;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.UConversion;
import pe.gob.onp.thaqhiri.util.USesion;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/visit-plans")
@Tag(name = "Plan de visitas", description = "Gestión de plan de visitas y verificación")
public class VisitPlanController {

    private static final Logger log = LoggerFactory.getLogger(VisitPlanController.class);

    private final VisitPlanService visitPlanService;
    private final VisitPlanImportService visitPlanImportService;
    private final MeterRegistry meterRegistry;

    public VisitPlanController(
            VisitPlanService visitPlanService,
            VisitPlanImportService visitPlanImportService,
            MeterRegistry meterRegistry
    ) {
        this.visitPlanService = visitPlanService;
        this.visitPlanImportService = visitPlanImportService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping
    @Operation(
            summary = "Crear plan de visitas y asignar colaborador",
            description = "Crea un plan de visitas y lo asigna a un colaborador con sus ítems asociados"
    )
    public VisitPlanResponse createPlan(@Valid @RequestBody VisitPlanRequest request,
    		                            HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:58 UTC-5 (Lima)][desc: Fuerza usuarioSesion desde token][obj: VisitPlanController.createPlan]
        VisitPlanRequest payload = new VisitPlanRequest(
                request.id(),
                request.verifierId(),
                request.title(),
                request.plannedFor(),
                request.items(),
                usuarioSesion
        );
        
        return visitPlanService.createPlan(payload, usuarioSesion, terminalSesion);
    }
    
    @PutMapping
    @Operation(
            summary = "Actualizar plan de visitas y asignar colaborador",
            description = "Actualiza los datos del plan de visitas y su asignación"
    )
    public VisitPlanResponse updatePlan(@Valid @RequestBody VisitPlanRequest request,
    		HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:58 UTC-5 (Lima)][desc: Fuerza usuarioSesion desde token][obj: VisitPlanController.updatePlan]
        VisitPlanRequest payload = new VisitPlanRequest(
                request.id(),
                request.verifierId(),
                request.title(),
                request.plannedFor(),
                request.items(),
                usuarioSesion
        );
        
        return visitPlanService.updatePlan(payload, usuarioSesion, terminalSesion);
    }

    @GetMapping("/{planId}")
    @Operation(
            summary = "Obtener plan de visitas por id",
            description = "Recupera el detalle completo de un plan de visitas por id"
    )
    public VisitPlanResponse getPlan(@PathVariable Long planId) {
        VisitPlanResponse resp = visitPlanService.getPlan(planId);
        log.info("Plan {} recuperado -> {}", planId, resp);
        return resp;
    }

    @GetMapping("/mine")
    @Operation(
            summary = "Obtener plan asignado al colaborador autenticado",
            description = "Devuelve el plan de visitas vigente para el usuario autenticado"
    )
    public VisitPlanResponse getPlanForVerifier(@AuthenticationPrincipal SaaPrincipal principal) {
        VisitPlanResponse resp = visitPlanService.getPlanForVerifier(principal);
        log.info("Plan para colaborador {} -> {}", principal.getName(), resp);
        return resp;
    }

    @PostMapping("/{planId}/items")
    @Operation(
            summary = "Agregar visita al plan",
            description = "Agrega un ítem de visita a un plan existente"
    )
    public VisitItemResponse addVisit(@PathVariable Long planId,
                                      @Valid @RequestBody VisitItemCreateRequest request,
                                      HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return visitPlanService.addItem(planId, request, usuarioSesion, terminalSesion);
    }

    @PatchMapping("/{planId}/items/reorder")
    @Operation(
            summary = "Reordenar visitas del plan",
            description = "Reordena los ítems de un plan según el orden proporcionado"
    )
    public VisitPlanResponse reorder(@PathVariable Long planId,
                                     @Valid @RequestBody VisitItemsReorderRequest request,
                                     HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return visitPlanService.reorderItems(planId, request, usuarioSesion, terminalSesion);
    }

    @PatchMapping("/items/{itemId}/state")
    @Operation(
            summary = "Actualizar estado de una visita",
            description = "Cambia el estado de un ítem de visita (llegada, en sitio, en visita, etc.)"
    )
    public VisitItemResponse updateState(@PathVariable Long itemId,
                                         @Valid @RequestBody VisitItemStateChangeRequest request,
                                         @AuthenticationPrincipal SaaPrincipal principal,
                                         HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return visitPlanService.updateState(itemId, request, principal, usuarioSesion, terminalSesion);
    }

    @PostMapping("/items/{itemId}/complete")
    @Operation(
            summary = "Completar visita con formulario de verificación",
            description = "Finaliza la visita registrando datos del formulario de verificación"
    )
    public VisitItemResponse complete(@PathVariable Long itemId,
                                      @Valid @RequestBody VisitItemCompleteRequest request,
                                      @AuthenticationPrincipal SaaPrincipal principal,
                                      HttpServletRequest httpRequest) {
    	
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        return visitPlanService.completeVisit(itemId, request, principal, usuarioSesion, terminalSesion);
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Lista visitas pendientes de reprogramar para supervisor][obj: VisitPlanController.buscarPendientesReprogramar]
    @GetMapping("/pending-reprogramar")
    @Operation(
            summary = "Listar visitas pendientes de reprogramar",
            description = "Lista visitas pendientes de reprogramar para supervisores"
    )
    public ResponseEntity<RespuestaBusquedaDTO<VisitItemPendingReprogramResponse>> buscarPendientesReprogramar(
            @RequestParam(required = false) String idPersona,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPlan,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanioPagina,
            HttpServletRequest httpRequest
    ) {
        log.info("Entramos a buscarPendientesReprogramar");
        log.info("idPersona: "+idPersona);
        log.info("fechaPlan: "+fechaPlan);
        log.info("pagina: "+pagina);
        log.info("tamanioPagina: "+tamanioPagina);
        
        try {
        	String usuarioSesion = USesion.resolveUsuario();
        	String terminalSesion = USesion.determineHost(httpRequest);
        	
        	//Por ahora acá se cambia el estado de los que quedaron pendientes, deberia estar en el job.
        	visitPlanService.marcarPendientesReprogramar(usuarioSesion, terminalSesion);
        	
        	//Luego se realiza la busqueda
            log.info("Llamando a VisitPlanService.buscarPendientesReprogramar");
            
            RespuestaBusquedaDTO<VisitItemPendingReprogramResponse> respuesta = visitPlanService.buscarPendientesReprogramar(
            		idPersona,
                    fechaPlan,
                    pagina,
                    tamanioPagina,
                    usuarioSesion,
                    terminalSesion
            );
            
            log.info("Respuesta de VisitPlanService.buscarPendientesReprogramar total={} items={}",
                    respuesta.getTotalRegistros(), respuesta.getResultados() != null ? respuesta.getResultados().size() : 0);
            
            return ResponseEntity.ok(respuesta);
        } catch (Exception ex) {
            log.error("Error en buscarPendientesReprogramar", ex);
            throw ex;
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 15:01 UTC-5 (Lima)][desc: Reprograma una visita pendiente y registra historial][obj: VisitPlanController.reprogramarVisita]
    @PostMapping("/pending-reprogramar/{itemId}/reassign")
    @Operation(
            summary = "Reprogramar visita pendiente",
            description = "Reasigna una visita pendiente de reprogramación y registra el historial"
    )
    public ResponseEntity<RespuestaDTO<VisitItemPendingReprogramResponse>> reprogramarVisita(
            @PathVariable Long itemId,
            @Valid @RequestBody VisitItemReassignRequest request,
            @AuthenticationPrincipal SaaPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        long start = System.nanoTime();
        String status = "success";
        
        try {        	
        	String usuarioSesion = USesion.resolveUsuario();
        	String terminalSesion = USesion.determineHost(httpRequest);
        	
            VisitItemPendingReprogramResponse resultado = visitPlanService.reprogramarItem(itemId, request, principal, usuarioSesion, terminalSesion);
            
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_EXITOSO,
                    "Reprogramacion realizada correctamente.",
                    List.of(resultado)
            ));
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordPlanMetric("reprogramar", status, System.nanoTime() - start);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:13 UTC-5 (Lima)][desc: Lista colaboradores con plan para fecha dada][obj: VisitPlanController.listarColaboradoresConPlan]
    @GetMapping("/verificadores-con-plan")
    @Operation(
            summary = "Listar verificadores con plan",
            description = "Lista colaboradores que tienen un plan NO COMPLETADO para una fecha dada"
    )
    public ResponseEntity<RespuestaDTO<UserResponse>> listarColaboradoresConPlanNoCompletado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPlan,
            @RequestParam(required = false) String idPersona
    ) {
    	
        List<UserResponse> resultados = visitPlanService.listarColaboradoresConPlanNoCompletado(
                fechaPlan,
                idPersona
        );
        
        log.info("listarColaboradoresConPlan fechaPlan={} idPersona={} total={}",
                fechaPlan, idPersona, resultados != null ? resultados.size() : 0);
        
        String codigo = resultados.isEmpty()
                ? "" + UConstante.RESULTADO_NO_ENCONTRO_FILAS
                : "" + UConstante.RESULTADO_SI_ENCONTRO_FILAS;
        
        String mensaje = resultados.isEmpty()
                ? "No se encontraron colaboradores con plan."
                : "Busqueda exitosa.";
        
        return ResponseEntity.ok(new RespuestaDTO<>(
                codigo,
                mensaje,
                resultados
        ));
    }
    
    
    /**
     * Buscar planes de visita con filtros opcionales
     * @param idEquipo filtro opcional
     * @param idPersona filtro opcional
     * @param fechaPlan filtro opcional (yyyy-MM-dd)
     * @return Lista de planes de visita
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar planes de visita",
            description = "Busca planes de visita con filtros opcionales y paginación"
    )
    public ResponseEntity<RespuestaBusquedaDTO<VisitPlanResponse>> buscarPlanes(
            @RequestParam(required = false) String idPersona,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPlan,
            @RequestParam(defaultValue = "1") int pagina,
    		@RequestParam(defaultValue = "10") int tamanioPagina,
    		@RequestParam(defaultValue = "asc") String orden,
    		@RequestParam(defaultValue = "verifier.id") String columnaOrden
    ) {    	
    	RespuestaBusquedaDTO<VisitPlanResponse> respuesta = visitPlanService.buscarPlanes(idPersona, fechaPlan,
    			                                                      pagina, tamanioPagina, orden, columnaOrden);    	
    	return ResponseEntity.ok(respuesta);        
    }
    
    
    @GetMapping("/existe")
    @Operation(
            summary = "Verificar existencia de plan",
            description = "Valida si existe un plan para una persona en una fecha específica"
    )
    public ResponseEntity<RespuestaDTO<String>> evaluarExistePlan(
            @RequestParam(required = true) Long idPersona,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPlan) {
    	
    	boolean resultado = visitPlanService.existePlanParaPersonaEnFecha(idPersona, fechaPlan);

    	RespuestaDTO<String> respuesta = new RespuestaDTO<>();
        
        if (!resultado) {
    		respuesta.setCodigoResultado("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("No se encontraron planes con los criterios especificados.");
    	} else {
    		respuesta.setCodigoResultado("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("Búsqueda exitosa.");
    	}
    	
        respuesta.setResultados(List.of());
        
    	return ResponseEntity.ok(respuesta);        
    }
    
    @GetMapping("/existe-otro-id")
    @Operation(
            summary = "Verificar existencia de plan excluyendo id",
            description = "Valida existencia de plan excluyendo un id específico"
    )
    public ResponseEntity<RespuestaDTO<String>> evaluarExistePlan(
            @RequestParam(required = true) Long idPersona,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPlan,
            @RequestParam(required = true) Long idPlanExcluido) {
    	
    	boolean resultado = visitPlanService.existePlanParaPersonaEnFechaOtroId(idPersona, fechaPlan, idPlanExcluido);

    	RespuestaDTO<String> respuesta = new RespuestaDTO<>();
        
        if (!resultado) {
    		respuesta.setCodigoResultado("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("No se encontraron planes con los criterios especificados.");
    	} else {
    		respuesta.setCodigoResultado("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS);
    		respuesta.setMensajeResultado("Búsqueda exitosa.");
    	}
    	
        respuesta.setResultados(List.of());
        
    	return ResponseEntity.ok(respuesta);        
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 10:14 UTC-5 (Lima)][desc: Descarga de plantilla Excel para carga masiva de planes de visita][obj: VisitPlanController.downloadImportTemplate]
    @GetMapping("/import/template")
    @Operation(
            summary = "Descargar plantilla de importación",
            description = "Descarga plantilla Excel para carga masiva de planes de visita"
    )
    public ResponseEntity<byte[]> downloadImportTemplate() {
        long start = System.nanoTime();
        String status = "success";
        byte[] bytes = visitPlanImportService.generarPlantilla();
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plantilla-planes-visita.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordPlanMetric("plan_template", status, System.nanoTime() - start);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:20 UTC-5 (Lima)][desc: Exporta a Excel los planes/visitas existentes filtrando por equipo/persona/fecha][obj: VisitPlanController.exportExcel]
    @GetMapping("/export/excel")
    @Operation(
            summary = "Exportar planes de visita",
            description = "Exporta planes/visitas a Excel según filtros"
    )
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String idPersona,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPlan
    ) {
        long start = System.nanoTime();
        String status = "success";
        try {
            byte[] bytes = visitPlanImportService.exportarExcel(idPersona, fechaPlan);
            String file = "planes-visita.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordPlanMetric("plan_export", status, System.nanoTime() - start);
        }
    }


    
    
    @PostMapping(value = "/import/excel", consumes = "multipart/form-data")
    @Operation(
            summary = "Importar planes desde Excel",
            description = "Importa planes de visita desde un archivo Excel"
    )
    public ResponseEntity<RespuestaDTO<VisitPlanValidaResultDTO>> importExcel(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal SaaPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        long start = System.nanoTime();
        String status = "success";
        
        String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
        
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:58 UTC-5 (Lima)][desc: Toma usuarioSesion desde token][obj: VisitPlanController.importExcel]
        try {
            VisitPlanValidaResultDTO result = visitPlanImportService.importExcel(file, usuarioSesion, terminalSesion);
            
            return ResponseEntity.ok(new RespuestaDTO<>(
                    "" + UConstante.RESULTADO_EXITOSO,
                    "Importación ejecutada.",
                    List.of(result)
            ));
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordPlanMetric("plan_import", status, System.nanoTime() - start);
        }
    }
    
    @PostMapping(value = "/masivo/guardar", consumes = "application/json")
    @Operation(
            summary = "Guardar visitas masivas",
            description = "Registra visitas masivas en base a un payload JSON"
    )
    public ResponseEntity<RespuestaDTO<Void>> guardarMasivo(@Valid @RequestBody VisitaPlanMasivoRequest request,
                                                            @AuthenticationPrincipal SaaPrincipal principal,
                                                            HttpServletRequest httpRequest) {
    	long start = System.nanoTime();
        String status = "success";
    	
        String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);        
        
    	String codigoRespuesta = "" + UConstante.RESULTADO_EXITOSO; //Valor pre-determinado
        
        try {
            ResultadoValidacion resultado = visitPlanService.registrarVisitasMasivas(request, usuarioSesion, terminalSesion);
        
            if(resultado.getTipoResultado() == TipoResultadoValidacion.CON_ERRORES) {        	
            	codigoRespuesta = "" + UConstante.RESULTADO_NO_PASA_VALIDACION;
            }
            
            return ResponseEntity.ok(new RespuestaDTO<>(
            		    codigoRespuesta,
    	                resultado.getMensaje(),
    	                List.of()
    	        ));        
        } catch (Exception ex) {
            status = "error";
            throw ex;
        } finally {
            recordPlanMetric("plan_masivo", status, System.nanoTime() - start);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Registra métricas de import/export/reprogramación de planes][obj: VisitPlanController.recordPlanMetric]
    private void recordPlanMetric(String action, String status, long durationNs) {
        Tags tags = Tags.of("action", action, "status", status);
        meterRegistry.counter("thaqhiri_backend_plan_ops_total", tags).increment();
        Timer.builder("thaqhiri_backend_plan_ops_duration")
                .tags(tags)
                .register(meterRegistry)
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);
        if ("error".equalsIgnoreCase(status)) {
            meterRegistry.counter("thaqhiri_backend_plan_ops_errors_total", Tags.of("action", action)).increment();
        }
    }
    
    
    @DeleteMapping("/eliminar/{id}")
    @Operation(
            summary = "Eliminar plan de visitas",
            description = "Realiza eliminación lógica del plan de visitas por id"
    )
    public ResponseEntity<RespuestaDTO<Void>> eliminar(@PathVariable Long id,
                                                       @AuthenticationPrincipal SaaPrincipal principal,
                                                       HttpServletRequest httpRequest) {

    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);

        String msgEliminado = visitPlanService.eliminar(id, usuarioSesion, terminalSesion);        

        if (msgEliminado == null) {
            return ResponseEntity.ok(new RespuestaDTO<>("0", "Plan eliminado correctamente.", List.of()));
        }
        else {
        	//No se pudo eliminar
        	return ResponseEntity.ok(new RespuestaDTO<>("1", msgEliminado, List.of()));
        }        
    }

    
}
