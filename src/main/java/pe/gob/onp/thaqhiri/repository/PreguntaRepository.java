package pe.gob.onp.thaqhiri.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.entity.Pregunta;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {

    List<Pregunta> findByIdCuestionarioAndEstadoOrderByOrden(Long idCuestionario, Integer estado);


    @Modifying
    @Transactional
    @Query("""
            UPDATE Pregunta p
               SET p.estado = 0,
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
    @Transactional
    @Query("""
            UPDATE Pregunta p
               SET p.estado = 0,
                   p.usuarioModi = :usuario,
                   p.terminalModi = :terminal
             WHERE p.idCuestionario = :idCuestionario
               AND p.estado = 1
           """)
    int desactivarPorCuestionario(
            @Param("idCuestionario") Long idCuestionario,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );


    @Query("""
            SELECT COALESCE(MAX(p.orden), 0)
            FROM Pregunta p
            WHERE p.estado = 1 AND
                  p.idCuestionario = :idCuestionario
            """)
    Integer findMaxOrdenByCuestionario(@Param("idCuestionario") Long idCuestionario);
}
