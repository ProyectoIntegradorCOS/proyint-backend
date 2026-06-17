package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "PREGUNTA")
public class Pregunta {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pregunta_seq")
    @SequenceGenerator(name = "pregunta_seq", sequenceName = "seq_pregunta", allocationSize = 1)
    @Column(name = "ID_PREG")
	private Long id;
	
    @Column(name = "ID_CUES", nullable = false)
    private Long idCuestionario;

    @Column(name = "DE_PREG", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "TI_PREG", nullable = false, length = 50)
    private String tipo;

    @Column(name = "IN_OBLI", nullable = false)
    private String obligatorio;

    @Column(name = "NU_ORDE", nullable = false)
    private Integer orden;

    @Column(name = "DE_GRUP", length = 100)
    private String grupo;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Pregunta.estado]
    @Column(name = "ST_REGI", nullable = false)
    private Integer estado;

    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCrea;

    @Column(name = "FE_USUA_CREA", nullable = true, insertable = false, updatable = false)
    private Date fechaCrea;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCrea;
    
    @Column(name = "ID_USUA_MODI", nullable = true, insertable = false, length = 30)
    private String usuarioModi;

    @Column(name = "FE_USUA_MODI", nullable = true, insertable = false, updatable = false)
    private Date fechaModi;

    @Column(name = "DE_TERM_MODI", nullable = true, insertable = false, length = 30)
    private String terminalModi;
    
    @Column(name = "ID_SIGU_PREG")
    private Long idSiguientePregunta;
    

	

	public Pregunta() {
		super();
		// TODO Auto-generated constructor stub
	}

	
    
    

    
}
