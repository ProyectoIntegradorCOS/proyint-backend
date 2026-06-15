package pe.gob.onp.thaqhiri.metrics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:51 UTC-5 (Lima)][desc: Endpoints protegidos para métricas UI y móvil][obj: MetricsController]
@Tag(name = "Metrics", description = "Registro de métricas desde frontend y app")
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Registrar métricas web", description = "Registra eventos y tiempos desde la web")
    @PostMapping("/ui")
    public ResponseEntity<Void> registrarUi(@RequestBody MetricEventRequest request) {
        metricsService.recordEvent(request, "web");
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Registrar métricas móvil", description = "Registra eventos y tiempos desde la app móvil")
    @PostMapping("/mobile")
    public ResponseEntity<Void> registrarMobile(@RequestBody MetricEventRequest request) {
        metricsService.recordEvent(request, "mobile");
        return ResponseEntity.accepted().build();
    }
}
