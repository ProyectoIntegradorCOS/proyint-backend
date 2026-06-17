package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "HORARIO")
public class Horario {

    @Id
    @Column(name = "ID_HORA")
    private Long id;

    @Column(name = "NO_HORA", nullable = false, length = 100)
    private String nombre;

    @Column(name = "NU_INI", nullable = false)
    private Integer horaInicio;

    @Column(name = "NU_FIN", nullable = false)
    private Integer horaFin;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Horario.estado]
    @Column(name = "ST_REGI", nullable = false)
    private Integer estado;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String idUsuaCrea;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date feUsuaCrea;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String deTermCrea;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String idUsuaModi;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private LocalDateTime feUsuaModi;

    @Column(name = "DE_TERM_MODI", length = 30)
    private String deTermModi;

	public Horario() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}
