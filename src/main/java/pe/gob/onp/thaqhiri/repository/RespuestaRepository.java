package pe.gob.onp.thaqhiri.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.entity.Respuesta;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Long> {
	
	
	// Obtener la respuesta de una pregunta específica
    Optional<Respuesta> findByIdPreguntaAndEstado(Long idPregunta, String estado);

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-18 UTC-5 (Lima)][desc: findFirst evita IncorrectResultSizeDataAccessException si hay duplicados (ej: doble clic en Completar)][obj: RespuestaRepository.findFirstByIdPreguntaAndIdItemAndEstado]
    Optional<Respuesta> findFirstByIdPreguntaAndIdItemAndEstadoOrderByIdDesc(Long idPregunta, Long idItem, String estado);
    
    
	@Modifying
    @Transactional
    @Query("""
            UPDATE Respuesta p
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
	
	
	@Query("""
		       SELECT r
		         FROM Respuesta r
		        WHERE r.idPersona = :idPersona
		          AND r.idPregunta IN (SELECT p.id FROM Pregunta p WHERE p.idCuestionario = :idCuestionario)
		        ORDER BY r.id ASC
		       """)
		List<Respuesta> findByPersonaAndCuestionarioOrdenadas(
		        @Param("idPersona") Long idPersona,
		        @Param("idCuestionario") Long idCuestionario
		);

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Lista respuestas por item de visita][obj: RespuestaRepository.findByItemOrdenadas]
    @Query("""
           SELECT r
             FROM Respuesta r
            WHERE r.idItem = :idItem
              AND r.estado = :estado
            ORDER BY r.id ASC
           """)
    List<Respuesta> findByItemOrdenadas(
            @Param("idItem") Long idItem,
            @Param("estado") String estado
    );

}
