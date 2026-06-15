package pe.gob.onp.thaqhiri.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class BackendMetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    public BackendMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationNs = System.nanoTime() - start;
            String method = request.getMethod();
            int status = response.getStatus();
            String normalizedPath = normalizePath(path);

            Tags tags = Tags.of(
                    "method", method,
                    "path", normalizedPath,
                    "status", Integer.toString(status)
            );

            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Registra métricas HTTP propias con prefijo thaqhiri_backend_][obj: BackendMetricsFilter]
            Counter.builder("thaqhiri_backend_http_requests_total")
                    .tags(tags)
                    .register(meterRegistry)
                    .increment();

            Timer.builder("thaqhiri_backend_http_request_duration")
                    .tags(tags)
                    .register(meterRegistry)
                    .record(durationNs, TimeUnit.NANOSECONDS);

            if (status >= 500) {
                Counter.builder("thaqhiri_backend_endpoint_errors_total")
                        .tags("method", method, "path", normalizedPath)
                        .register(meterRegistry)
                        .increment();
            }
        }
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "unknown";
        }
        String normalized = path.replaceAll("/[0-9]+", "/{id}");
        return normalized.replaceAll(
                "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
                "/{uuid}"
        );
    }
}
