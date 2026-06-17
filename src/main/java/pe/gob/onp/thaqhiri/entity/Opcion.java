package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "OPCION")
public class Opcion {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opcion_seq")
    @SequenceGenerator(name = "opcion_seq", sequenceName = "seq_opcion", allocationSize = 1)
    @Column(name = "ID_OPCI")
	private Long id;
	
    @Column(name = "ID_PREG", nullable = false)
    private Long idPregunta;

    @Column(name = "DE_OPCI", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "VL_OPCI", length = 50)
    private String valor;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Opcion.estado]
    @Column(name = "ST_REGI", nullable = false)
    private Integer estado;
    
    @Column(name = "NU_ORDE", nullable = false)
    private Integer orden;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCrea;

    @Column(name = "FE_USUA_CREA", nullable = true, insertable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaCrea;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCrea;

    @Column(name = "ID_USUA_MODI", nullable = true, insertable = false, length = 30)
    private String usuarioModi;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 09:54 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: LocationTrackerApp supportedLocales]
    @Column(name = "FE_USUA_MODI", nullable = true, insertable = false, updatable = false, length = 30)
    @Temporal(TemporalType.DATE)
    private Date fechaModi;

    @Column(name = "DE_TERM_MODI", nullable = true, insertable = false, length = 30)
    private String terminalModi;
    
    @Column(name = "ID_SIGU_PREG")
    private Long idSiguientePregunta;
    
	public Opcion() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}
