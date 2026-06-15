package pe.gob.onp.thaqhiri.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onp.thaqhiri.entity.Cuestionario;
import pe.gob.onp.thaqhiri.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
	
    @Modifying
    @Transactional
    @Query("""
            UPDATE Cuestionario p
               SET p.estado = '0',
                   p.usuarioModi = :usuario,
                   p.terminalModi = :terminal
             WHERE p.id = :id
           """)
    int desactivar(
            @Param("id") Long id,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );
    
    
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 12:21 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: CuestionarioRepository.buscarPaginado trim]
    @Query("""
            SELECT c
            FROM Cuestionario c
            WHERE c.estado = '1'
              AND (
                   :nombre IS NULL OR
                   TRIM(:nombre) = '' OR
                   UPPER(c.nombre) LIKE CONCAT('%', UPPER(TRIM(:nombre)), '%')
              )
            """)
        Page<Cuestionario> buscarPaginado(
                @Param("nombre") String nombre,
                Pageable pageable
        );

    @Query("""
    	    SELECT c
    	    FROM Cuestionario c
    	    JOIN Equipo e ON e.idCuestionario = c.id
    	    WHERE e.id = :idEquipo
    	      AND c.estado = '1'
    	""")
    	Optional<Cuestionario> buscarActivoPorEquipo(
    	        @Param("idEquipo") Long idEquipo
    	);


    @Query("""
    	    SELECT CASE 
    	           WHEN COUNT(e) > 0 THEN true 
    	           ELSE false 
    	           END
    	    FROM Equipo e
    	    WHERE e.estado = '1' AND
    	          e.idCuestionario = :idCuestionario
    	""")
    	boolean existeAsignacionAEquipo(
    	        @Param("idCuestionario") Long idCuestionario
    	);

    
    List<Cuestionario> findByEstadoOrderByNombre(String estado);
    
}
