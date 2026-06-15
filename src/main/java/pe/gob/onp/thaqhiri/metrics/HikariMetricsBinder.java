package pe.gob.onp.thaqhiri.metrics;

import com.zaxxer.hikari.HikariDataSource;
// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:29 UTC-5 (Lima)][desc: Corrige import de HikariPoolMXBean][obj: HikariMetricsBinder]
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class HikariMetricsBinder {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;

    public HikariMetricsBinder(DataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Expone métricas Hikari con prefijo thaqhiri_backend_][obj: HikariMetricsBinder]
    public void bindHikariMetrics() {
        if (!(dataSource instanceof HikariDataSource hikariDataSource)) {
            return;
        }
        HikariPoolMXBean pool = hikariDataSource.getHikariPoolMXBean();
        if (pool == null) {
            return;
        }
        meterRegistry.gauge("thaqhiri_backend_db_active_connections", pool, HikariPoolMXBean::getActiveConnections);
        meterRegistry.gauge("thaqhiri_backend_db_idle_connections", pool, HikariPoolMXBean::getIdleConnections);
        meterRegistry.gauge("thaqhiri_backend_db_total_connections", pool, HikariPoolMXBean::getTotalConnections);
        meterRegistry.gauge("thaqhiri_backend_db_threads_awaiting", pool, HikariPoolMXBean::getThreadsAwaitingConnection);
    }
}
