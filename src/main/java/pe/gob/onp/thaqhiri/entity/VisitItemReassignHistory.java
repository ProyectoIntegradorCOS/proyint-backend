package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "VISITA_ITEM_REASSIGN_HIST")
public class VisitItemReassignHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visita_item_reassign_hist_seq")
    @SequenceGenerator(
            name = "visita_item_reassign_hist_seq",
            sequenceName = "seq_visita_item_reassign_hist",
            allocationSize = 1
    )
    @Column(name = "ID_REAS_HIST")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ITEM", nullable = false)
    private VisitItem item;

    @Column(name = "ID_PLAN_ANTE", nullable = false)
    private Long previousPlanId;

    @Column(name = "ID_PLAN_NUEV", nullable = false)
    private Long newPlanId;

    @Column(name = "ID_PERS_ANTE", nullable = false)
    private Long previousVerifierId;

    @Column(name = "ID_PERS_NUEV", nullable = false)
    private Long newVerifierId;

    @Column(name = "FE_PLAN_ANTE", nullable = false)
    private LocalDate previousPlannedFor;

    @Column(name = "FE_PLAN_NUEV", nullable = false)
    private LocalDate newPlannedFor;

    @Column(name = "DE_MOTI", length = 500)
    private String reason;

    @Column(name = "ID_USUA_CREA", length = 30, nullable = false)
    private String createdBy;

    @Column(name = "DE_TERM_CREA", length = 30, nullable = false)
    private String createdFrom;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Auditoria en historial de reprogramacion][obj: VisitItemReassignHistory.createdAt]
    @Column(name = "FE_USUA_CREA", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Estado logico en historial de reprogramacion][obj: VisitItemReassignHistory.stRegi]
    @Column(name = "ST_REGI", nullable = false)
    private String stRegi;

}
