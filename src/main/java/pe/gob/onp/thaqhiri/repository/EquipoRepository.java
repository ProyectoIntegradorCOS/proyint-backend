package pe.gob.onp.thaqhiri.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.gob.onp.thaqhiri.entity.Equipo;
import pe.gob.onp.thaqhiri.entity.User;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
	
	/**
     * Busca todos los equipos que tengan estado = '1' y los ordena por nombre.
     * Genera la consulta: SELECT e FROM Equipo e WHERE e.estado = '1' ORDER BY e.nombre ASC
     * @param estado El estado de vigencia ('1' para activo).
     * @return Lista de entidades Equipo ordenadas por nombre.
     */
    List<Equipo> findByEstadoOrderByNombreAsc(String estado);
    
    /**
     * Recupera la lista de equipos supervisados por el usuario recorriendo toda la jerarquia de forma recursiva.
     * @param idSupervisor
     * @return
     */
    @Query(
    		value = """
    			-- 1. CTE: Simplificado para solo obtener los IDs de los equipos en la jerarquía
    			WITH equipos_rec (ID_EQUI, ID_SUPE) AS (
    				-- NIVEL 1: equipos donde el usuario es supervisor
    				SELECT e.ID_EQUI, e.ID_SUPE
    				FROM EQUIPO_TRABAJO e
    				-- [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: EquipoRepository.findEquiposBySupervisorJerarquia ST_REGI]
    				WHERE e.ID_SUPE = :idSupervisor AND
    					  e.ST_REGI = '1'

    				UNION ALL

    				-- NIVELES SIGUIENTES
    				SELECT e2.ID_EQUI, e2.ID_SUPE
    				FROM EQUIPO_TRABAJO e2
    				JOIN PERSONAL p
    				  ON e2.ID_SUPE = p.ID_PERS
    				JOIN equipos_rec er
    				  ON p.ID_EQUI = er.ID_EQUI
    				-- [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: EquipoRepository.findEquiposBySupervisorJerarquia ST_REGI personal]
    				WHERE e2.ST_REGI = '1' AND
    					  p.ST_REGI = '1'
    			)
    			
    			-- 2. SELECT FINAL: Hace JOIN con la tabla física para seleccionar TODAS las columnas (e_full.*)
    			-- Esto asegura que todos los campos requeridos por la Entidad Equipo (incluyendo auditoría y IN_VISI) existan en el ResultSet.
    			SELECT 
    				e_full.* FROM 
    				EQUIPO_TRABAJO e_full
    			JOIN
    				(SELECT DISTINCT ID_EQUI FROM equipos_rec) er_ids
    					ON e_full.ID_EQUI = er_ids.ID_EQUI
    			""",
    		nativeQuery = true
    	)
    	List<Equipo> findBySupervisorOrderByNombreAsc(@Param("idSupervisor") Long idSupervisor);
    
    
    @Query("""
            SELECT e
            FROM Equipo e
            LEFT JOIN e.supervisor u
            WHERE e.estado = '1'
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
               SET e.estado = '0',
                   e.usuarioModificacion = :usuario,
                   e.terminalModificacion = :terminal
             WHERE e.id = :id
           """)
    int eliminarEquipo(
            @Param("id") Long id,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );
    
    
    boolean existsByIdAndEstado(Long id, String estado);
    
    List<Equipo> findByNombreAndEstado(String nombre, String estado);
    
    /**
     * Busca si existe el equipo para otro id.
     * @param nombre
     * @param id
     * @return
     */
    @Query("""
 	       SELECT e
 	       FROM Equipo e
 	       WHERE e.nombre = :nombre
 	         AND e.id <> :id
 	         AND e.estado = '1'
 	       """)
 	List<Equipo> findByNombreAndIdNot(
 			@Param("nombre") String nombre,
 	        @Param("id") Integer id 	        
 	);
    
    
}
