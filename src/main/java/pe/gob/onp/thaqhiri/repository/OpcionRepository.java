package pe.gob.onp.thaqhiri.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.entity.Opcion;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Long> {
	
	// Buscar todas las opciones de una pregunta específica
    List<Opcion> findByIdPreguntaAndEstadoOrderByOrden(Long idPregunta, String estado);
    
    
	@Modifying
    @Transactional
    @Query("""
            UPDATE Opcion p
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
	
	
	@Modifying
	@Query("""
	    update Opcion o
	    set o.estado = '0',
	        o.usuarioModi = :usuario,
	        o.terminalModi = :terminal
	    where o.idPregunta = :idPregunta
	      and o.estado = '1'
	""")
	void desactivarPorPregunta(@Param("idPregunta") Long idPregunta,
	                           @Param("usuario") String usuario,
	                           @Param("terminal") String terminal);

	
}
