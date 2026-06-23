package pe.gob.onp.thaqhiri.service;

import lombok.extern.slf4j.Slf4j;
import pe.gob.onp.thaqhiri.controller.LocationController;
import pe.gob.onp.thaqhiri.dto.DailyDistanceResponse;
import pe.gob.onp.thaqhiri.dto.LocationCreateRequest;
import pe.gob.onp.thaqhiri.dto.LocationHistoryResponse;
import pe.gob.onp.thaqhiri.dto.LocationResponse;
import pe.gob.onp.thaqhiri.entity.Location;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.repository.LocationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

@Service
@Transactional
@Slf4j
public class LocationService {

	private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private static final int MAX_STALE_SECONDS = 7200;
    private static final double STILL_SPEED_MPS = 1.0;
    private static final double WALKING_SPEED_MPS = 2.5;
    private static final int MIN_INTERVAL_STILL_SECONDS = 30;
    private static final int MIN_INTERVAL_WALKING_SECONDS = 5;
    private static final int MIN_INTERVAL_VEHICLE_SECONDS = 3;
    private static final double MIN_DISTANCE_STILL_METERS = 10.0;
    private static final double MIN_DISTANCE_WALKING_METERS = 12.0;
    private static final double MIN_DISTANCE_VEHICLE_METERS = 40.0;
    private static final double MAX_SPEED_WALKING_MPS = 2.0; // ~7.2 km/h
    private static final double MAX_SPEED_VEHICLE_MPS = 27.7777777778; // 100 km/h
    private static final double MAX_JUMP_WALKING_METERS = 120.0;
    private static final double MAX_JUMP_VEHICLE_METERS = 500.0;
    private static final int MAX_JUMP_WINDOW_SECONDS = 10;
    private static final int FORCE_ACCEPT_SECONDS = 300;
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-26 12:00 UTC-5 (Lima)][desc: Se reemplaza MAX_ACCURACY estático por umbrales dinámicos][obj: LocationService.evaluateFilter dynamic accuracy]
    
    private final LocationRepository locationRecordRepository;
    private final UserService userService;

    public LocationService(LocationRepository locationRecordRepository,
                           UserService userService) {
        this.locationRecordRepository = locationRecordRepository;
        this.userService = userService;
    }

    public LocationResponse create(LocationCreateRequest request, String usuario, String terminal) {
        User user = userService.getEntityBySaaSubject(request.saaSubject());
        return createForUser(request, user, usuario, terminal);
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-05 08:20 UTC-5 (Lima)][desc: Implementa creación por lotes (batch)][obj: LocationService.createBatch]
    public int createBatch(pe.gob.onp.thaqhiri.dto.LocationBatchRequest request, String usuario, String terminal) {
        if (request.locations().isEmpty()) return 0;
        
        // Asumimos que todas son para el mismo usuario (el del primer elemento) o validamos cada una.
        // Por eficiencia y seguridad, validaremos que el usuario exista una vez si es posible, 
        // pero el DTO LocationCreateRequest tiene saaSubject en cada item.
        // Agrupamos por saaSubject para minimizar consultas a BD.
        
        var locationsBySubject = request.locations().stream()
                .collect(Collectors.groupingBy(LocationCreateRequest::saaSubject));
        
        int totalSaved = 0;
        
        for (var entry : locationsBySubject.entrySet()) {
            String saaSubject = entry.getKey();
            List<LocationCreateRequest> requests = entry.getValue();
            User user = userService.getEntityBySaaSubject(saaSubject);
            if (user == null) {
                throw new RuntimeException("Usuario no encontrado: " + saaSubject);
            }
            List<LocationCreateRequest> ordered = requests.stream()
                    .sorted(Comparator.comparing(LocationCreateRequest::timestamp))
                    .collect(Collectors.toList());
            FilterContext lastAccepted = loadLastAccepted(user);
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Traza de guardado batch a BD por usuario][obj: LocationService.createBatch]
            log.info("TRACE DB batch persist start saaSub={} count={}", saaSubject, requests.size());
            List<Location> entities = new ArrayList<>();
            for (LocationCreateRequest req : ordered) {
                FilterDecision decision = evaluateFilter(req, lastAccepted);
                Location entity = toEntity(req, user, usuario, terminal, decision);
                entities.add(entity);
                if (!decision.filteredOut()) {
                    lastAccepted = FilterContext.from(req);
                }
            }
            
            var saved = locationRecordRepository.saveAll(entities);
            int savedCount = 0;
            Long firstId = null;
            for (Location l : saved) {
                savedCount++;
                if (firstId == null) firstId = l.getId();
            }
            totalSaved += entities.size();
            log.info("TRACE DB batch persist SUCCESS: {} de {} ubicaciones guardadas en BD para saaSub={} (Primer ID={})",
                    savedCount, requests.size(), saaSubject, firstId);
        }
        
        log.debug("Batch guardado: {} ubicaciones procesadas", totalSaved);
        return totalSaved;
    }

    private LocationResponse createForUser(LocationCreateRequest request, User user, String usuario, String terminal) {
        log.debug("Guardando ubicación para usuario {}", user.getSaaSub());
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Traza de guardado individual a BD][obj: LocationService.createForUser]
        log.info("TRACE DB save start saaSub={} ts={}", request.saaSubject(), request.timestamp());
        FilterContext lastAccepted = loadLastAccepted(user);
        FilterDecision decision = evaluateFilter(request, lastAccepted);
        Location record = toEntity(request, user, usuario, terminal, decision);
        Location saved = locationRecordRepository.save(record);
        log.info("TRACE DB save ok saaSub={} id={} insertedAt={}",
                request.saaSubject(), saved.getId(), saved.getInsertdAt());
        log.debug("Ubicación persistida con id={}", saved.getId());
        return toResponse(saved);
    }

    private Location toEntity(LocationCreateRequest request, User user, String usuario, String terminal, FilterDecision decision) {
        Location record = new Location();
        record.setUser(user);
        record.setLatitude(request.latitude());
        record.setLongitude(request.longitude());
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-18 00:00 UTC-5 (Lima)][desc: Normaliza timestamp a zona America/Lima antes de persistir FE_UBIC][obj: LocationService.toEntity Lima]
        record.setRecordedAt(normalizeToLima(request.timestamp()));
        record.setAccuracy(request.accuracy());
        record.setAltitude(request.altitude());
        record.setSpeed(request.speed());
        record.setHeading(request.heading());
        record.setBatteryLevel(request.batteryLevel());
        record.setActivityType(request.activityType());
        record.setCreatedBy(usuario);
        record.setCreatedFrom(terminal);
        record.setFilteredOut(decision.filteredOut());
        record.setFilteredReason(decision.reason());
        return record;
    }

    private OffsetDateTime normalizeToLima(OffsetDateTime ts) {
        if (ts == null) return null;
        return ts.atZoneSameInstant(ZoneId.of("America/Lima")).toOffsetDateTime();
    }

    @Transactional(readOnly = true)
    public LocationHistoryResponse getHistory(String saaSubject, OffsetDateTime start, OffsetDateTime end) {
        User user = userService.getEntityBySaaSubject(saaSubject);
        log.debug("Obteniendo historial para uid={} entre {} y {}", saaSubject, start, end);

        List<LocationResponse> points;
        try {
            points = locationRecordRepository
                    .findByUserAndRecordedAtBetweenFiltered(user, start, end)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } catch (Exception ex) {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-17 15:09 UTC-5 (Lima)][desc: Evita 500 si falla consulta de historial; retorna lista vacía y distancia 0][obj: LocationService.getHistory points fallback]
            log.error("Fallo consultando historial para uid={} (retornando lista vacía)", saaSubject, ex);
            points = List.of();
        }

        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-17 15:09 UTC-5 (Lima)][desc: Calcula distancia en Java para evitar dependencia SDO_GEOM en BD][obj: LocationService.getHistory distance java]
        double distanceKm = computeDistanceKm(points);
        log.debug("Historial obtenido: puntos={} distanciaKm={}", points.size(), distanceKm);

        return new LocationHistoryResponse(saaSubject, start, end, points, distanceKm);
    }

    @Transactional(readOnly = true)
    public DailyDistanceResponse getDailyDistance(String saaSubject, LocalDate date) {
        User user = userService.getEntityBySaaSubject(saaSubject);
        log.debug("Calculando distancia diaria para uid={} fecha={}", saaSubject, date);
        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusDays(1);

        List<LocationResponse> points = locationRecordRepository
                .findByUserAndRecordedAtBetweenFiltered(user, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-17 15:09 UTC-5 (Lima)][desc: Calcula distancia diaria en Java para evitar SDO_GEOM][obj: LocationService.getDailyDistance distance java]
        double distanceKm = computeDistanceKm(points);
        log.debug("Distancia diaria calculada: {} km", distanceKm);

        return new DailyDistanceResponse(saaSubject, date, distanceKm);
    }

    private LocationResponse toResponse(Location record) {
        return new LocationResponse(
                record.getId(),
                record.getLatitude(),
                record.getLongitude(),
                record.getRecordedAt(),
                record.getAccuracy(),
                record.getAltitude(),
                record.getSpeed(),
                record.getHeading(),
                record.getBatteryLevel(),
                record.getActivityType()
        );
    }

    private double computeDistanceKm(List<LocationResponse> points) {
        if (points == null || points.size() < 2) return 0.0;
        double totalMeters = 0.0;
        LocationResponse prev = null;
        for (LocationResponse p : points) {
            if (p == null) continue;
            if (prev != null) {
                totalMeters += haversineMeters(
                        prev.latitude(),
                        prev.longitude(),
                        p.latitude(),
                        p.longitude()
                );
            }
            prev = p;
        }
        return totalMeters / 1000.0;
    }

    private FilterContext loadLastAccepted(User user) {
        List<Location> last = locationRecordRepository.findLastAccepted(user, PageRequest.of(0, 1));
        if (last.isEmpty()) return null;
        Location l = last.get(0);
        if (l.getRecordedAt() == null) return null;
        return new FilterContext(
                l.getLatitude(),
                l.getLongitude(),
                l.getRecordedAt().toInstant(),
                l.getSpeed(),
                l.getAccuracy()
        );
    }

    // Orden de aplicación de reglas (backend) y ejemplo breve:
    // 1) same_lat_lng_same_timestamp
    // 2) precision
    // 3) timestamp_no_creciente
    // 4) same_lat_lng_<10s
    // 5) min_dist_min_time
    // 6) force_accept (señal de vida, dt>=300s)
    // 7) salto_velocidad
    // 8) salto_ventana_corta
    // 9) antiguedad (puntos muy antiguos)
    //
    // Nota: force_accept (dt>=300s) permite reenganche antes de verificar velocidad.
    //
    // Regla 9: antiguedad. Si el punto llega muy tarde vs. el último aceptado, se descarta.
    //    Ejemplo: último aceptado 10:00:00, nuevo 10:04:10 (250s) -> RECHAZA.
    private FilterDecision evaluateFilter(LocationCreateRequest req, FilterContext prev) {
        Instant ts = req.timestamp().toInstant();
        Double accuracy = req.accuracy();
        // Usamos la velocidad que envía el celular, o asumimos detenido si no hay dato
        double currentSpeed = (req.speed() != null) ? req.speed() : 0.0;
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-17 00:00 UTC-5 (Lima)][desc: Homologa con móvil: si speed es null y hay punto previo, deriva velocidad de dist/dt para el umbral de precisión en lugar de asumir 0 (detenido)][obj: LocationService.evaluateFilter speedForAccuracy]
        double speedForAccuracy = currentSpeed;
        if (req.speed() == null && prev != null) {
            double earlyDt = (ts.toEpochMilli() - prev.timestampMs()) / 1000.0;
            double earlyDist = haversineMeters(prev.latitude(), prev.longitude(), req.latitude(), req.longitude());
            speedForAccuracy = (earlyDt > 0) ? earlyDist / earlyDt : 0.0;
        }
        double maxAllowedAccuracy = maxAccuracyMetersForSpeed(speedForAccuracy);

        // Regla 2: precision. Accuracy > umbral dinámico -> RECHAZA.
        // Ejemplo: accuracy=30m andando a pie (max 15m) -> RECHAZA.
        if (accuracy != null && accuracy > maxAllowedAccuracy) {
            log.info(
                    "FILTER_REJECT precision saaSub={} lat={} lng={} ts={} acc={} maxAcc={} dtSec={} speedMps={}",
                    req.saaSubject(),
                    req.latitude(),
                    req.longitude(),
                    req.timestamp(),
                    accuracy,
                    maxAllowedAccuracy,
                    prev == null ? null : (req.timestamp().toInstant().toEpochMilli() - prev.timestampMs()) / 1000.0,
                    speedForAccuracy
            );
            return FilterDecision.rejected("precision_" + accuracy);
        }

        if (prev != null) {
            long dtMillis = ts.toEpochMilli() - prev.timestampMs();
            double dt = dtMillis / 1000.0;
            boolean sameInstant = req.latitude().equals(prev.latitude())
                    && req.longitude().equals(prev.longitude())
                    && ts.toEpochMilli() == prev.timestampMs();
            // Regla 1: duplicado exacto (misma lat/lng + mismo timestamp con ms) -> RECHAZA.
            // Ejemplo: mismo punto reenviado por reintento con ts idéntico -> RECHAZA.
            if (sameInstant) {
                log.info(
                        "FILTER_REJECT same_lat_lng_same_timestamp saaSub={} lat={} lng={} ts={} dtSec={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dt,
                        null
                );
                return FilterDecision.rejected("same_lat_lng_same_timestamp");
            }
            // Regla 3: timestamp no creciente (dt <= 0) -> RECHAZA.
            // Ejemplo: llega un punto con hora anterior al último aceptado -> RECHAZA.
            if (dt <= 0) {
                log.info(
                        "FILTER_REJECT timestamp_no_creciente saaSub={} lat={} lng={} ts={} dtSec={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dt,
                        null
                );
                return FilterDecision.rejected("timestamp_no_creciente");
            }
            // Regla 4: misma lat/lng en menos de 10s -> RECHAZA.
            // Ejemplo: dt=6s, lat/lng igual al anterior -> RECHAZA.
            if (req.latitude().equals(prev.latitude())
                    && req.longitude().equals(prev.longitude())
                    && dt < 10.0) {
                log.info(
                        "FILTER_REJECT same_lat_lng_<10s saaSub={} lat={} lng={} ts={} dtSec={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dt,
                        null
                );
                return FilterDecision.rejected("same_lat_lng_<10s");
            }

            double dist = haversineMeters(prev.latitude(), prev.longitude(), req.latitude(), req.longitude());
            double derivedSpeed = (req.speed() != null && req.speed() > 0) ? req.speed() : (dist / dt);
            int minInterval = minIntervalSecondsForSpeed(derivedSpeed);
            double minDistance = minDistanceMetersForSpeed(derivedSpeed);
            // Regla 5: min_dist_min_time. Poco movimiento en poco tiempo -> RECHAZA.
            // Ejemplo: dt=4s y dist=5m (caminando) -> RECHAZA.
            if (dt < minInterval && dist < minDistance) {
                log.info(
                        "FILTER_REJECT min_dist_min_time saaSub={} lat={} lng={} ts={} dist={} dtSec={} minDist={} minInt={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dist,
                        dt,
                        minDistance,
                        minInterval,
                        derivedSpeed
                );
                return FilterDecision.rejected("min_dist_min_time");
            }
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-17 00:00 UTC-5 (Lima)][desc: Mueve force_accept ANTES de salto_velocidad y salto_ventana_corta. Si dt>=300s se acepta como señal de vida sin importar la velocidad implícita (el gap largo hace inválida la comparación de velocidad).][obj: LocationService.evaluateFilter force_accept order]
            // Regla 6: force_accept (señal de vida). Si dt >= 300s, acepta sin verificar velocidad.
            if (dt >= FORCE_ACCEPT_SECONDS) {
                log.info(
                        "FILTER_FORCE_ACCEPT saaSub={} lat={} lng={} ts={} dtSec={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dt,
                        derivedSpeed
                );
                return FilterDecision.accepted();
            }
            double impliedSpeed = dist / dt;
            // Regla 7: salto_velocidad. Velocidad implícita mayor al máximo -> RECHAZA.
            // Ejemplo: dist=900m, dt=5s -> 180 m/s (irreal) -> RECHAZA.
            if (impliedSpeed > maxSpeedForSpeed(derivedSpeed)) {
                log.info(
                        "FILTER_REJECT salto_velocidad saaSub={} lat={} lng={} ts={} dist={} dtSec={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dist,
                        dt,
                        impliedSpeed
                );
                return FilterDecision.rejected("salto_velocidad");
            }
            // Regla 8: salto_ventana_corta. Salto grande en pocos segundos -> RECHAZA.
            // Ejemplo: dist=300m en 5s (ventana corta) -> RECHAZA.
            if (dt <= MAX_JUMP_WINDOW_SECONDS) {
                double maxJump = maxJumpMetersForSpeed(derivedSpeed);
                if (dist > maxJump) {
                    log.info(
                            "FILTER_REJECT salto_ventana_corta saaSub={} lat={} lng={} ts={} dist={} dtSec={} maxJump={} speedMps={}",
                            req.saaSubject(),
                            req.latitude(),
                            req.longitude(),
                            req.timestamp(),
                            dist,
                            dt,
                            maxJump,
                            derivedSpeed
                    );
                    return FilterDecision.rejected("salto_ventana_corta");
                }
            }

            // Regla 9: antiguedad. Si el punto llega muy tarde vs. el último aceptado, se descarta.
            // Ejemplo: último aceptado 10:00:00, nuevo 12:30:10 (9010s) -> RECHAZA.
            if (dt > MAX_STALE_SECONDS) {
                log.info(
                        "FILTER_REJECT antiguedad saaSub={} lat={} lng={} ts={} dtSec={} speedMps={}",
                        req.saaSubject(),
                        req.latitude(),
                        req.longitude(),
                        req.timestamp(),
                        dt,
                        derivedSpeed
                );
                return FilterDecision.rejected("antiguedad_" + Math.round(dt) + "s");
            }
        }
    log.info(
            "FILTER_ACCEPT saaSub={} lat={} lng={} ts={} dtSec={} speedMps={}",
            req.saaSubject(),
            req.latitude(),
            req.longitude(),
            req.timestamp(),
            prev == null ? null : (req.timestamp().toInstant().toEpochMilli() - prev.timestampMs()) / 1000.0,
            req.speed()
    );
    return FilterDecision.accepted();
    }

    private int minIntervalSecondsForSpeed(double speed) {
        if (speed <= STILL_SPEED_MPS) return MIN_INTERVAL_STILL_SECONDS;
        if (speed < WALKING_SPEED_MPS) return MIN_INTERVAL_WALKING_SECONDS;
        return MIN_INTERVAL_VEHICLE_SECONDS;
    }

    private double minDistanceMetersForSpeed(double speed) {
        if (speed <= STILL_SPEED_MPS) return MIN_DISTANCE_STILL_METERS;
        if (speed < WALKING_SPEED_MPS) return MIN_DISTANCE_WALKING_METERS;
        return MIN_DISTANCE_VEHICLE_METERS;
    }

    private double maxSpeedForSpeed(double speed) {
        if (speed < WALKING_SPEED_MPS) return MAX_SPEED_WALKING_MPS;
        return MAX_SPEED_VEHICLE_MPS;
    }

    private double maxJumpMetersForSpeed(double speed) {
        if (speed < WALKING_SPEED_MPS) return MAX_JUMP_WALKING_METERS;
        return MAX_JUMP_VEHICLE_METERS;
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-26 12:00 UTC-5 (Lima)][desc: Precisión dinámica basada en velocidad del dispositivo][obj: LocationService.maxAccuracyMetersForSpeed]
    private double maxAccuracyMetersForSpeed(double speed) {
        if (speed <= 1.0) return 20.0;    // Detenido / Caminando lento (0-3.6 km/h)
        if (speed <= 5.0) return 25.0;    // Caminando / Bici (3-18 km/h)
        if (speed <= 15.0) return 35.0;   // Tráfico urbano (18-54 km/h)
        return 50.0;                      // Carretera (54+ km/h)
    }

    private record FilterContext(
            Double latitude,
            Double longitude,
            Instant timestamp,
            Double speed,
            Double accuracy
    ) {
        long timestampMs() {
            return timestamp.toEpochMilli();
        }

        static FilterContext from(LocationCreateRequest req) {
            return new FilterContext(
                    req.latitude(),
                    req.longitude(),
                    req.timestamp().toInstant(),
                    req.speed(),
                    req.accuracy()
            );
        }
    }

    private record FilterDecision(boolean filteredOut, String reason) {
        static FilterDecision accepted() {
            return new FilterDecision(false, null);
        }

        static FilterDecision rejected(String reason) {
            return new FilterDecision(true, reason);
        }
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371000.0;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
    
    
    
    
}
