package pe.gob.onp.thaqhiri.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:51 UTC-5 (Lima)][desc: Registra métricas de UI y móvil en Micrometer][obj: MetricsService.recordEvent]
    public void recordEvent(MetricEventRequest request, String platformOverride) {
        String platform = normalize(platformOverride != null ? platformOverride : request.getPlatform(), "unknown");
        String screen = normalize(request.getScreen(), "unknown");
        String action = normalize(request.getAction(), "unknown");
        String status = normalize(request.getStatus(), "success");
        String version = normalize(request.getVersion(), "unknown");

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:58 UTC-5 (Lima)][desc: Prefija métricas con nombre del sistema][obj: MetricsService.recordEvent prefix]
        String prefix = "thaqhiri_" + ("web".equalsIgnoreCase(platform) ? "ui" : platform.toLowerCase());
        Tags tags = Tags.of(
                "screen", screen,
                "action", action,
                "status", status,
                "platform", platform,
                "version", version
        );

        Counter.builder(prefix + "_events_total")
                .tags(tags)
                .register(meterRegistry)
                .increment();

        if ("error".equalsIgnoreCase(status)) {
            Counter.builder(prefix + "_errors_total")
                    .tags(tags)
                    .register(meterRegistry)
                    .increment();
        }

        Long durationMs = request.getDurationMs();
        if (durationMs != null && durationMs > 0) {
            Timer.builder(prefix + "_action_duration")
                    .tags(tags)
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);
        }
    }

    private String normalize(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}
