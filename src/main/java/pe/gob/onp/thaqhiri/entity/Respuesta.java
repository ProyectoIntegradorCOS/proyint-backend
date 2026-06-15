package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "RESPUESTA")
public class Respuesta {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "respuesta_seq")
    @SequenceGenerator(name = "respuesta_seq", sequenceName = "seq_respuesta", allocationSize = 1)
    @Column(name = "ID_RESP")
	private Long id;

    @Column(name = "ID_PERS", nullable = false)
    private Long idPersona;
    
    @Column(name = "ID_CUES", nullable = false)
    private Long idCuestionario;

    @Column(name = "ID_PREG", nullable = false)
    private Long idPregunta;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Vincula respuestas con el item de visita para mostrar respuestas por visita][obj: Respuesta.idItem]
    @Column(name = "ID_ITEM", nullable = true)
    private Long idItem;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Vincula respuestas con el plan de visitas para trazabilidad][obj: Respuesta.idPlan]
    @Column(name = "ID_PLAN", nullable = true)
    private Long idPlan;

    @Column(name = "DE_PREG", length = 1000)
    private String pregunta;
    
    @Column(name = "VL_RESP", length = 1000)
    private String respuesta;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Respuesta.estado]
    @Column(name = "ST_REGI", nullable = false)
    private String estado;

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

	public Respuesta() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(Long idPersona) {
		this.idPersona = idPersona;
	}

	public Long getIdPregunta() {
		return idPregunta;
	}

	public void setIdPregunta(Long idPregunta) {
		this.idPregunta = idPregunta;
	}

	public Long getIdItem() {
		return idItem;
	}

	public void setIdItem(Long idItem) {
		this.idItem = idItem;
	}

	public Long getIdPlan() {
		return idPlan;
	}

	public void setIdPlan(Long idPlan) {
		this.idPlan = idPlan;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getUsuarioCrea() {
		return usuarioCrea;
	}

	public void setUsuarioCrea(String usuarioCrea) {
		this.usuarioCrea = usuarioCrea;
	}

	public Date getFechaCrea() {
		return fechaCrea;
	}

	public void setFechaCrea(Date fechaCrea) {
		this.fechaCrea = fechaCrea;
	}

	public String getTerminalCrea() {
		return terminalCrea;
	}

	public void setTerminalCrea(String terminalCrea) {
		this.terminalCrea = terminalCrea;
	}

	public String getUsuarioModi() {
		return usuarioModi;
	}

	public void setUsuarioModi(String usuarioModi) {
		this.usuarioModi = usuarioModi;
	}

	public Date getFechaModi() {
		return fechaModi;
	}

	public void setFechaModi(Date fechaModi) {
		this.fechaModi = fechaModi;
	}

	public String getTerminalModi() {
		return terminalModi;
	}

	public void setTerminalModi(String terminalModi) {
		this.terminalModi = terminalModi;
	}

	public Long getIdCuestionario() {
		return idCuestionario;
	}

	public void setIdCuestionario(Long idCuestionario) {
		this.idCuestionario = idCuestionario;
	}

	public String getPregunta() {
		return pregunta;
	}

	public void setPregunta(String pregunta) {
		this.pregunta = pregunta;
	}

    
}
