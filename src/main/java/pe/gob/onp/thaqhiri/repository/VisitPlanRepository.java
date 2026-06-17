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

    Optional<VisitPlan> findByIdAndStRegi(Long id, Integer stRegi);

    Optional<VisitPlan> findFirstByVerifierIdAndStatusNotOrderByCreatedAtDesc(Long verifierId, VisitPlanStatus excludedStatus);

    Optional<VisitPlan> findFirstByVerifierIdAndStatusNotAndPlannedForOrderByCreatedAtDesc(
            Long verifierId,
            VisitPlanStatus excludedStatus,
            LocalDate plannedFor);

    Optional<VisitPlan> findFirstByVerifierIdAndPlannedForOrderByCreatedAtDesc(
            Long verifierId,
            LocalDate plannedFor);

    Optional<VisitPlan> findFirstByVerifierIdAndPlannedForAndStRegiOrderByCreatedAtDesc(
            Long verifierId,
            LocalDate plannedFor,
            Integer stRegi);

    @Query("""
            SELECT p
            FROM VisitPlan p
            JOIN p.verifier v
            LEFT JOIN v.equipo e
            WHERE p.stRegi = 1
              AND (:fechaPlan IS NULL OR p.plannedFor = :fechaPlan)
              AND (:idPersonas IS NULL OR v.id IN :idPersonas)
            """)
    Page<VisitPlan> buscarPlanes(
            @Param("idPersonas") java.util.Collection<Long> idPersonas,
            @Param("fechaPlan") LocalDate fechaPlan,
            Pageable pageable
    );


    boolean existsByVerifierIdAndPlannedForAndStRegi(Long verifierId, LocalDate plannedFor, Integer stRegi);

    boolean existsByVerifierIdAndPlannedForAndIdNotAndStRegi(
            Long verifierId,
            LocalDate plannedFor,
            Long idPlanExcluido,
            Integer stRegi
    );


    @Modifying
    @Transactional
    @Query("""
            UPDATE VisitPlan p
               SET p.stRegi = 0,
                   p.updatedBy = :usuario,
                   p.updatedFrom = :terminal
             WHERE p.id = :id
           """)
    int eliminarPlan(
            @Param("id") Long id,
            @Param("usuario") String usuario,
            @Param("terminal") String terminal
    );

    @Query("""
            SELECT DISTINCT p.verifier
            FROM VisitPlan p
            WHERE p.stRegi = 1
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
