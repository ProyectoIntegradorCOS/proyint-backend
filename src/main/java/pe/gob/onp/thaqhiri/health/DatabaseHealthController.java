package pe.gob.onp.thaqhiri.health;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping({"/api/health", "/api/healths"})
@Slf4j
public class DatabaseHealthController {

	private static final Logger log = LoggerFactory.getLogger(DatabaseHealthController.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseStatus() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM dual", Integer.class);
            log.debug("Health-check DB ejecutado, resultado={}", result);
            return ResponseEntity.ok(Map.of("status", "UP", "db", result));
        } catch (Exception ex) {
            log.warn("Health-check DB falló", ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "DOWN", "error", "Servicio de base de datos no disponible"));
        }
    }
}
