package pe.gob.onp.thaqhiri.entity;

import java.time.OffsetDateTime;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.gob.onp.thaqhiri.util.UConstante;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "UBICACION")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ubicacion_seq")
    @SequenceGenerator(name = "ubicacion_seq", sequenceName = "seq_ubicacion", allocationSize = 1)
    @Column(name = "ID_UBIC")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PERS", nullable = false)
    private User user;

    @Column(name = "NU_LATI")
    private Double latitude;

    @Column(name = "NU_LONG")
    private Double longitude;

    @Column(name = "NU_PREC")
    private Double accuracy;

    @Column(name = "NU_ALTI")
    private Double altitude;

    @Column(name = "NU_VELO")
    private Double speed;

    @Column(name = "NU_DIRE")
    private Double heading;

    @Column(name = "NU_NIVE_BATE")
    private Integer batteryLevel;

    @Column(name = "TI_ACTI")
    private String activityType;

    @Column(name = "FE_UBIC", nullable = false)
    private OffsetDateTime recordedAt;
    
    @Column(name = "FE_USUA_CREA", insertable = false, updatable = false)
    private OffsetDateTime insertdAt;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Location.status]
    @Column(name = "ST_REGI", nullable = false, insertable = false)
    private String status;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String createdBy;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String createdFrom;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String updatedBy;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "DE_TERM_MODI", length = 30)
    private String updatedFrom;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-26 00:00 UTC-5 (Lima)][desc: Marca si el punto fue filtrado por reglas de tracking][obj: Location.filteredOut]
    @Column(name = "FL_FILT")
    private Boolean filteredOut;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-26 00:00 UTC-5 (Lima)][desc: Guarda la regla aplicada al filtrar un punto][obj: Location.filteredReason]
    @Column(name = "DE_FILT", length = 200)
    private String filteredReason;

}
