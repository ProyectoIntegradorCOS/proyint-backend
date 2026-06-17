package pe.gob.onp.thaqhiri.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.onp.thaqhiri.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySaaSub(String saaSub);

    Optional<User> findByUsuarioAndEstado(String usuario, Integer estado);

    @Query("""
            SELECT u
            FROM User u
            LEFT JOIN u.equipo e
            WHERE u.estado = 1
              AND u.tipoTrabajo = 1
              AND (:nombres IS NULL OR UPPER(u.nombre) LIKE CONCAT('%', UPPER(CAST(:nombres AS string)), '%'))
              AND (:equipoId IS NULL OR e.id = CAST(:equipoId AS integer))
            """)
    Page<User> buscarUsersCampoPaginado(
            @Param("nombres") String nombres,
            @Param("equipoId") String equipoId,
            Pageable pageable
    );

    List<User> findByEstadoOrderByNombreAsc(Integer estado);

    List<User> findByEstadoAndTipoTrabajoOrderByNombreAsc(Integer estado, Integer tipoTrabajo);

    List<User> findByEquipoIdAndEstadoAndTipoTrabajo(Long idEquipo, Integer estado, Integer tipoTrabajo);

    List<User> findByEquipoIdAndEstadoAndTipoTrabajoOrderByNombreAsc(Long idEquipo, Integer estado, Integer tipoTrabajo);

    @Query(
        value = """
            SELECT DISTINCT p.*
            FROM PERSONAL p
            JOIN EQUIPO_TRABAJO e
              ON p.ID_EQUI = e.ID_EQUI
            WHERE e.ID_SUPE = :idSupervisor
              AND p.ST_REGI = 1
            ORDER BY p.NO_PERS ASC
            """,
        nativeQuery = true
    )
    List<User> findPersonasBySupervisor(@Param("idSupervisor") Long idSupervisor);

    @Query("""
               SELECT u
               FROM User u
               WHERE u.usuario = :usuario
                 AND u.id <> :id
                 AND u.estado = 1
               """)
    List<User> findByUsuarioAndIdNot(
            @Param("id") Long id,
            @Param("usuario") String usuario
    );

    @Query("SELECT u.passwordHash FROM User u WHERE UPPER(u.usuario) = UPPER(CAST(:usuario AS string)) AND u.estado = 1")
    Optional<String> findPasswordHashByUsuario(@Param("usuario") String usuario);
}
