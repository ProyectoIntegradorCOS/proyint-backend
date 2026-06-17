package pe.gob.onp.thaqhiri.repository;

import pe.gob.onp.thaqhiri.entity.Location;
import pe.gob.onp.thaqhiri.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("""
            SELECT l
            FROM Location l
            WHERE l.user = :user
            AND l.recordedAt >= :start
              AND l.recordedAt <= :end
              AND l.filteredOut = false
            ORDER BY l.recordedAt ASC
            """)
    List<Location> findByUserAndRecordedAtBetweenFiltered(
            @Param("user") User user,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("""
            SELECT l
            FROM Location l
            WHERE l.user = :user
            AND l.filteredOut = false
              AND l.status = 1
              AND l.recordedAt = (
                SELECT MAX(l2.recordedAt)
                FROM Location l2
                WHERE l2.user = :user
                AND l2.filteredOut = false
                AND l2.status = 1
                )
            ORDER BY l.id DESC
            FETCH FIRST 1 ROWS ONLY
            """)
    List<Location> findLastAccepted(@Param("user") User user, Pageable pageable);

    /*@Query(value = """
            WITH ordered AS (
                -- [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-19 00:00 UTC-5 (Lima)][desc: Evita ORA-22901: no usar LAG sobre SDO_GEOMETRY; usa lat/lon y construye puntos][obj: LocationRepository.calculateDistanceMeters]
                SELECT NU_LATI AS lat,
                       NU_LONG AS lon,
                       LAG(NU_LATI) OVER (ORDER BY FE_UBIC) AS prev_lat,
                       LAG(NU_LONG) OVER (ORDER BY FE_UBIC) AS prev_lon
                FROM UBICACION
                WHERE ID_PERS = :personalId
                  AND FE_UBIC >= :start
                  AND FE_UBIC < :end
            )
            SELECT NVL(SUM(
                SDO_GEOM.SDO_DISTANCE(
                    SDO_GEOMETRY(2001, 4326, SDO_POINT_TYPE(lon, lat, NULL), NULL, NULL),
                    SDO_GEOMETRY(2001, 4326, SDO_POINT_TYPE(prev_lon, prev_lat, NULL), NULL, NULL),
                    0.005,
                    'unit=M'
                )
            ), 0)
            FROM ordered
            WHERE prev_lat IS NOT NULL AND prev_lon IS NOT NULL
            """, nativeQuery = true)
    double calculateDistanceMeters(@Param("personalId") Long personalId,
                                   @Param("start") OffsetDateTime start,
                                   @Param("end") OffsetDateTime end);*/
    
    
    
    
}
