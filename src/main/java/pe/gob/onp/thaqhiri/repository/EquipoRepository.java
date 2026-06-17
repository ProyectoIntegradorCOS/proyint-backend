package pe.gob.onp.thaqhiri.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.onp.thaqhiri.entity.Equipo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByEstadoOrderByNombreAsc(Integer estado);

    @Query(
        value = """
            WITH equipos_rec (ID_EQUI, ID_SUPE) AS (
                SELECT e.ID_EQUI, e.ID_SUPE
                FROM EQUIPO_TRABAJO e
                WHERE e.ID_SUPE = :idSupervisor AND
                      e.ST_REGI = 1

                UNION ALL

                SELECT e2.ID_EQUI, e2.ID_SUPE
                FROM EQUIPO_TRABAJO e2
                JOIN PERSONAL p
                  ON e2.ID_SUPE = p.ID_PERS
                JOIN equipos_rec er
                  ON p.ID_EQUI = er.ID_EQUI
                WHERE e2.ST_REGI = 1 AND
                      p.ST_REGI = 1
            )
            SELECT e_full.* FROM EQUIPO_TRABAJO e_full
            JOIN (SELECT DISTINCT ID_EQUI FROM equipos_rec) er_ids
              ON e_full.ID_EQUI = er_ids.ID_EQUI
            """,
        nativeQuery = true
    )
    List<Equipo> findBySupervisorOrderByNombreAsc(@Param("idSupervisor") Long idSupervisor);


    @Query("""
            SELECT e
            FROM Equipo e
            LEFT JOIN e.supervisor u
            WHERE e.estado = 1
              AND (:nombreEquipo IS NULL OR UPPER(e.nombre) LIKE CONCAT('%', UPPER(:nombreEquipo), '%'))
              AND (:nombreSupervisor IS NULL OR UPPER(u.nombre) LIKE CONCAT('%', UPPER(:nombreSupervisor), '%'))
            """)
    Page<Equipo> buscarEquiposPaginado(
            @Param("nombreEquipo") String nombreEquipo,
            @Param("nombreSupervisor") String nombreSupervisor,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE Equipo e
               SET e.estado = 0,
                   e.usuarioModificacion = :usuario,
                   e.terminalModificacion = :terminal
             WHERE e.id = :id
           """)
    int eliminarEquipo(
            @Param("id") Long id,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );


    boolean existsByIdAndEstado(Long id, Integer estado);

    List<Equipo> findByNombreAndEstado(String nombre, Integer estado);

    @Query("""
           SELECT e
           FROM Equipo e
           WHERE e.nombre = :nombre
             AND e.id <> :id
             AND e.estado = 1
           """)
    List<Equipo> findByNombreAndIdNot(
            @Param("nombre") String nombre,
            @Param("id") Integer id
    );
}
