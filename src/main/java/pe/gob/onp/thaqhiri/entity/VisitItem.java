package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.onp.thaqhiri.model.VisitItemPriority;
import pe.gob.onp.thaqhiri.model.VisitItemState;
import pe.gob.onp.thaqhiri.util.OffsetDateTimeConverter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "VISITA_ITEM")
public class VisitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visita_item_seq")
    @SequenceGenerator(name = "visita_item_seq", sequenceName = "seq_visita_item", allocationSize = 1)
    @Column(name = "ID_ITEM")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PLAN", nullable = false)
    private VisitPlan plan;

    @Column(name = "NO_DEST", nullable = false, length = 200)
    private String companyName;

    @Column(name = "FE_META")
    @Convert(converter = OffsetDateTimeConverter.class)
    private OffsetDateTime targetTime;

    @Column(name = "NU_ORDE", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_ITEM", nullable = false, length = 30)
    private VisitItemState state;

    @Column(name = "FE_INI")
    private OffsetDateTime startTime;

    @Column(name = "FE_FIN")
    private OffsetDateTime endTime;

    @Column(name = "DE_INFO", length = 500)
    private String otherInfo;

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
    
    @Column(name = "DE_DIRE", length = 100)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEST")
    private Destino destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_PRIO", nullable = false, length = 20)
    private VisitItemPriority priority;

    @Column(name = "DE_PLANT_PV", length = 100)
    private String pvTemplate;


}
