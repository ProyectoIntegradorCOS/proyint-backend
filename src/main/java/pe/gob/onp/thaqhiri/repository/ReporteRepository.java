package pe.gob.onp.thaqhiri.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.onp.thaqhiri.dto.ReporteProductividadDTO;
import pe.gob.onp.thaqhiri.entity.VisitPlan;
import pe.gob.onp.thaqhiri.model.VisitItemState;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<VisitPlan, Long> {

    @Query("""
    	    SELECT new pe.gob.onp.thaqhiri.dto.ReporteProductividadDTO(
    	        v.id,
    	        v.nombre,
    	        e.nombre,
    	        COUNT(vi.id),                                            
    	        SUM(CASE WHEN vi.endTime IS NOT NULL THEN 1 ELSE 0 END),
    	        SUM(
	                CASE
	                    WHEN vi.endTime IS NOT NULL
	                     AND EXISTS (
	                         SELECT 1
	                         FROM Respuesta r
	                         WHERE r.idItem = vi.id
	                           AND LOWER(r.pregunta) = LOWER('¿Se culminó con la atención del PV?')
	                           AND LOWER(r.respuesta) = LOWER('Sí') 
	                     )
	                    THEN 1 ELSE 0
	                END
	            ),
    	        SUM(CASE WHEN vi.endTime IS NULL THEN 1 ELSE 0 END),
    	        TO_CHAR(vp.plannedFor, 'YYYY-MM-DD'),
    	        vp.id,
    	        vp.status
    	    )
    	    FROM VisitPlan vp
    	    JOIN vp.verifier v
    	    LEFT JOIN v.equipo e
    	    JOIN VisitItem vi ON vi.plan.id = vp.id
    	    WHERE vp.stRegi = '1'
    	      AND vi.state <> :estadoExcluido
    	      AND (:idPersonas IS NULL OR v.id IN :idPersonas)
    	      AND (:fechaInicio IS NULL OR vp.plannedFor >= :fechaInicio)
    	      AND (:fechaFin IS NULL OR vp.plannedFor <= :fechaFin)
    	    GROUP BY
    	        v.id,
    	        v.nombre,
    	        e.nombre,
    	        vp.plannedFor,
    	        vp.id,
    	        vp.status
    	    ORDER BY TO_CHAR(vp.plannedFor, 'YYYY-MM-DD'), e.nombre, v.nombre
    	""")
    	List<ReporteProductividadDTO> obtenerReporteProductividad(
    			@Param("idPersonas") java.util.Collection<Long> idPersonas,
    	        LocalDate fechaInicio,
    	        LocalDate fechaFin,
    	        VisitItemState estadoExcluido
    	);


}
