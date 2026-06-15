package pe.gob.onp.thaqhiri.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.entity.VisitPlan;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.model.VisitPlanStatus;
import java.time.LocalDate;
import java.util.Optional;

public interface VisitPlanRepository extends JpaRepository<VisitPlan, Long> {

    Optional<VisitPlan> findByIdAndStRegi(Long id, String stRegi);

    Optional<VisitPlan> findFirstByVerifierIdAndStatusNotOrderByCreatedAtDesc(Long verifierId, VisitPlanStatus excludedStatus);
    
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-12 12:25 UTC-5 (Lima)][desc: Obtiene plan del día para verificador excluyendo completados][obj: VisitPlanRepository]
    Optional<VisitPlan> findFirstByVerifierIdAndStatusNotAndPlannedForOrderByCreatedAtDesc(
            Long verifierId,
            VisitPlanStatus excludedStatus,
            LocalDate plannedFor);

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-08 13:56 UTC-5 (Lima)][desc: Obtiene el plan del día del verificador incluyendo completados][obj: VisitPlanRepository]
    Optional<VisitPlan> findFirstByVerifierIdAndPlannedForOrderByCreatedAtDesc(
            Long verifierId,
            LocalDate plannedFor);

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-06 00:00 UTC-5 (Lima)][desc: Obtiene plan del día del verificador solo activos (st_regi=1)][obj: VisitPlanRepository]
    Optional<VisitPlan> findFirstByVerifierIdAndPlannedForAndStRegiOrderByCreatedAtDesc(
            Long verifierId,
            LocalDate plannedFor,
            String stRegi);
        
    @Query("""
            SELECT p
            FROM VisitPlan p
            JOIN p.verifier v
            LEFT JOIN v.equipo e
            WHERE p.stRegi = '1'              
              AND (:fechaPlan IS NULL OR p.plannedFor = :fechaPlan)
              AND (:idPersonas IS NULL OR v.id IN :idPersonas)
            """)
    Page<VisitPlan> buscarPlanes(
            @Param("idPersonas") java.util.Collection<Long> idPersonas,
            @Param("fechaPlan") LocalDate fechaPlan,
            Pageable pageable
    );

    
    boolean existsByVerifierIdAndPlannedForAndStRegi(Long verifierId, LocalDate plannedFor, String stRegi);
    
    boolean existsByVerifierIdAndPlannedForAndIdNotAndStRegi(
            Long verifierId,
            LocalDate plannedFor,
            Long idPlanExcluido,
            String stRegi
    );
    
    
    @Modifying
    @Transactional
    @Query("""
            UPDATE VisitPlan p
               SET p.stRegi = '0',
                   p.updatedBy = :usuario,
                   p.updatedFrom = :terminal
             WHERE p.id = :id
           """)
    int eliminarPlan(
            @Param("id") Long id,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 08:13 UTC-5 (Lima)][desc: Lista colaboradores con plan por fecha][obj: VisitPlanRepository.findVerifiersWithPlan]
    @Query("""
            SELECT DISTINCT p.verifier
            FROM VisitPlan p
            WHERE p.stRegi = '1'
              AND p.status != :status
              AND p.plannedFor = :fechaPlan
              AND (:verifierIds IS NULL OR p.verifier.id IN :verifierIds)
            """)
    java.util.List<User> findVerifiersWithPlanNotCompleted(
            @Param("fechaPlan") java.time.LocalDate fechaPlan,
            @Param("verifierIds") java.util.Collection<Long> verifierIds,
            @Param("status") VisitPlanStatus status            
    );
    
    
}
