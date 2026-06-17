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
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pe.gob.onp.thaqhiri.entity.Horario;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PERSONAL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personal_seq")
    @SequenceGenerator(name = "personal_seq", sequenceName = "seq_personal", allocationSize = 1)
    @Column(name = "ID_PERS")
    private Long id;

    @Column(name = "SAA_SUB", unique = true, length = 255)
    private String saaSub;

    @Column(name = "NO_PERS", nullable = false, length = 100)
    private String nombre;

    @Column(name = "LG_USUA", nullable = false, length = 30)
    private String usuario;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: User.estado]
    @Column(name = "ST_REGI", nullable = false)
    private Integer estado;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCreacion;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCreacion;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String usuarioModificacion;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime fechaModificacion;

    @Column(name = "DE_TERM_MODI", length = 30)
    private String terminalModificacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EQUI", referencedColumnName = "ID_EQUI")
    private Equipo equipo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_HORA", referencedColumnName = "ID_HORA")
    private Horario horario;    
    
    @Column(name = "TI_TRAB", nullable = false)
    private Integer tipoTrabajo;

    @Column(name = "PASSWORD_HASH", length = 255)
    private String passwordHash;

}
