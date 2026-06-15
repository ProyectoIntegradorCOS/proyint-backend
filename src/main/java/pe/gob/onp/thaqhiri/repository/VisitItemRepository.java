package pe.gob.onp.thaqhiri.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import pe.gob.onp.thaqhiri.entity.VisitItem;
import pe.gob.onp.thaqhiri.model.VisitItemState;

import java.util.Collection;
import java.util.List;
import java.time.LocalDate;

public interface VisitItemRepository extends JpaRepository<VisitItem, Long> {

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 09:57 UTC-5 (Lima)][desc: Agrega consultas que excluyen items eliminados lógicamente][obj: VisitItemRepository]
    List<VisitItem> findByPlanIdAndStateNotOrderByOrderIndexAsc(Long planId, VisitItemState state);

    List<VisitItem> findByPlanIdAndStateNot(Long planId, VisitItemState state);

    List<VisitItem> findByPlanIdOrderByOrderIndexAsc(Long planId);

    List<VisitItem> findByPlanVerifierIdAndStateIn(Long verifierId, Collection<VisitItemState> states);

    long countByPlanVerifierIdAndState(Long verifierId, VisitItemState state);
    
    @Modifying
    @Transactional
    @Query("UPDATE VisitItem vi SET vi.state = :nuevoEstado WHERE vi.plan.id = :planId")
    int actualizarEstadoPorPlanId(Long planId, VisitItemState nuevoEstado);
    
    List<VisitItem> findByPlanId(Long planId);


    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:44 UTC-5 (Lima)][desc: Busca visitas pendientes a reprogramar para admin sin jerarquia][obj: VisitItemRepository.findReprogramablesAdmin]
    @Query("""
            SELECT i
            FROM VisitItem i                 
            JOIN i.plan p
            WHERE i.state = :state
              AND (:fechaPlan IS NULL OR p.plannedFor = :fechaPlan)
              AND (:idPersonas IS NULL OR p.verifier.id IN :idPersonas)
            """)
    org.springframework.data.domain.Page<VisitItem> findReprogramables(
    		@Param("idPersonas") java.util.Collection<Long> idPersonas,
            @Param("state") VisitItemState state,
            @Param("fechaPlan") LocalDate fechaPlan,
            org.springframework.data.domain.Pageable pageable
    );

        
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 07:44 UTC-5 (Lima)][desc: Busca pendientes vencidos para admin sin jerarquia][obj: VisitItemRepository.findPendientesParaReprogramarAdmin]
    @Query("""
            SELECT i
            FROM VisitItem i
            JOIN i.plan p
            WHERE p.plannedFor < :fechaCorte
              AND i.state IN :states
            """)
    List<VisitItem> findPendientesParaReprogramar(
            @Param("fechaCorte") LocalDate fechaCorte,
            @Param("states") Collection<VisitItemState> states
    );
    
}
