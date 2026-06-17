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
    
    Optional<User> findByUsuarioAndEstado(String usuario, String estado);
    
    
    @Query("""
            SELECT u
            FROM User u
            LEFT JOIN u.equipo e
            WHERE u.estado = '1'
              AND u.tipoTrabajo = 1
              AND (:nombres IS NULL OR UPPER(u.nombre) LIKE CONCAT('%', UPPER(:nombres), '%'))
              AND (:equipoId IS NULL OR e.id = CAST(:equipoId AS integer))
            """)
        Page<User> buscarUsersCampoPaginado(
                @Param("nombres") String nombres,
                @Param("equipoId") String equipoId,
                Pageable pageable
        );
    
    
    List<User> findByEstadoOrderByNombreAsc(String estado);
    
    List<User> findByEstadoAndTipoTrabajoOrderByNombreAsc(String estado, Integer tipoTrabajo);
    
    List<User> findByEquipoIdAndEstadoAndTipoTrabajo(Long idEquipo, String estado, Integer tipoTrabajo);
    
    List<User> findByEquipoIdAndEstadoAndTipoTrabajoOrderByNombreAsc(Long idEquipo, String estado, Integer tipoTrabajo);
    
    
    /**
     * Obtiene la lista de personas pertenecientes a los equipos
     * supervisados directamente por el supervisor,
     * ordenadas por nombre.
     */
    @Query(
        value = """
            SELECT DISTINCT p.*
            FROM PERSONAL p
            JOIN EQUIPO_TRABAJO e
              ON p.ID_EQUI = e.ID_EQUI
            WHERE e.ID_SUPE = :idSupervisor
              AND p.ST_REGI = '1'
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
    	         AND u.estado = '1'
    	       """)
    	List<User> findByUsuarioAndIdNot(
    	        @Param("id") Long id,
    	        @Param("usuario") String usuario
    	);

    @Query("SELECT u.passwordHash FROM User u WHERE UPPER(u.usuario) = UPPER(:usuario) AND u.estado = '1'")
    Optional<String> findPasswordHashByUsuario(@Param("usuario") String usuario);

}
