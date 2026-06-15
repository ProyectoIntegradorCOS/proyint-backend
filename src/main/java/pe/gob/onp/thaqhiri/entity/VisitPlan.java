package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.onp.thaqhiri.model.VisitPlanStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "VISITA_PLAN")
public class VisitPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visita_plan_seq")
    @SequenceGenerator(name = "visita_plan_seq", sequenceName = "seq_visita_plan", allocationSize = 1)
    @Column(name = "ID_PLAN")
    private Long id;

    /*
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SUPE", nullable = false)
    private User supervisor;
    */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_VERI", nullable = false)
    private User verifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_PLAN", nullable = false, length = 30)
    private VisitPlanStatus status;

    @Column(name = "DE_PLAN", length = 150)
    private String title;

    @Column(name = "FE_PLAN")
    private LocalDate plannedFor;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Guarda coordenadas y fecha de inicio real del plan][obj: VisitPlan start coords]
    @Column(name = "NU_LAT_INI")
    private Double startLatitude;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Guarda coordenadas y fecha de inicio real del plan][obj: VisitPlan start coords]
    @Column(name = "NU_LON_INI")
    private Double startLongitude;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Guarda fecha de inicio real del plan][obj: VisitPlan start coords]
    @Column(name = "FE_INI_PLAN")
    private OffsetDateTime startAt;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Guarda coordenadas y fecha de fin real del plan][obj: VisitPlan finish coords]
    @Column(name = "NU_LAT_FIN")
    private Double endLatitude;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Guarda coordenadas y fecha de fin real del plan][obj: VisitPlan finish coords]
    @Column(name = "NU_LON_FIN")
    private Double endLongitude;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Guarda fecha de fin real del plan][obj: VisitPlan finish coords]
    @Column(name = "FE_FIN_PLAN")
    private OffsetDateTime endAt;

    @Column(name = "ID_USUA_CREA", length = 30, nullable = false)
    private String createdBy;

    @Column(name = "DE_TERM_CREA", length = 30, nullable = false)
    private String createdFrom;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String updatedBy;

    @Column(name = "DE_TERM_MODI", length = 30)
    private String updatedFrom;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
    
    @Column(name = "ST_REGI")
    private String stRegi;

}
