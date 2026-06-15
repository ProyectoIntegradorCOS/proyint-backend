package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.onp.thaqhiri.model.VisitItemEventType;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "VISITA_ITEM_HIST")
public class VisitItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visita_item_hist_seq")
    @SequenceGenerator(name = "visita_item_hist_seq", sequenceName = "seq_visita_item_hist", allocationSize = 1)
    @Column(name = "ID_ITEM_HIST")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ITEM", nullable = false)
    private VisitItem item;

    @Enumerated(EnumType.STRING)
    @Column(name = "TI_EVEN", nullable = false, length = 30)
    private VisitItemEventType eventType;

    @Column(name = "DE_ANTE", length = 200)
    private String previousValue;

    @Column(name = "DE_NUEV", length = 200)
    private String newValue;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: insertable=true para poder grabar la fecha/hora real del evento desde el cliente (offline o en línea)][obj: VisitItemHistory.eventAt]
    @Column(name = "FE_EVEN", nullable = false, insertable = true, updatable = false)
    private OffsetDateTime eventAt;

    @Column(name = "ID_USUA_CREA", length = 30, nullable = false)
    private String createdBy;

    @Column(name = "DE_TERM_CREA", length = 30, nullable = false)
    private String createdFrom;

    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "ID_USUA_MODI", length = 30)
    private String updatedBy;

    @Column(name = "DE_TERM_MODI", length = 30)
    private String updatedFrom;

    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Guarda coordenadas del evento de visita en el histórico][obj: VisitItemHistory latitude/longitude]
    @Column(name = "NU_LATI")
    private Double latitude;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Guarda coordenadas del evento de visita en el histórico][obj: VisitItemHistory latitude/longitude]
    @Column(name = "NU_LONG")
    private Double longitude;

}
