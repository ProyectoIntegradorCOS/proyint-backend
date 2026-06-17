package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "CUESTIONARIO")
public class Cuestionario {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cuestionario_seq")
    @SequenceGenerator(name = "cuestionario_seq", sequenceName = "seq_cuestionario", allocationSize = 1)
    @Column(name = "ID_CUES")
	private Long id;	

    @Column(name = "NO_CUES", nullable = false, length = 200)
    private String nombre;

    @Column(name = "DE_CUES", length = 500)
    private String descripcion;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Cuestionario.estado]
    @Column(name = "ST_REGI", nullable = false)
    private Integer estado;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCrea;

    @Column(name = "FE_USUA_CREA", nullable = true, insertable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaCrea;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCrea;

    @Column(name = "ID_USUA_MODI", nullable = true, insertable = false, length = 30)
    private String usuarioModi;

    @Column(name = "FE_USUA_MODI", nullable = true, insertable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaModi;

    @Column(name = "DE_TERM_MODI",nullable = true, insertable = false, length = 30)
    private String terminalModi;

	public Cuestionario() {
		super();
		// TODO Auto-generated constructor stub
	}

    
}
