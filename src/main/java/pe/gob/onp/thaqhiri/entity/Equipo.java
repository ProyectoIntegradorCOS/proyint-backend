package pe.gob.onp.thaqhiri.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "equipo_trabajo")
public class Equipo {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "equipo_seq")
    @SequenceGenerator(name = "equipo_seq", sequenceName = "seq_equipo_trabajo", allocationSize = 1)
    @Column(name = "ID_EQUI")
    private Integer id;

    @Column(name = "NO_EQUI", length = 60, nullable = false)
    private String nombre;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SUPE", referencedColumnName = "ID_PERS")
    private User supervisor;

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: Equipo.estado]
    @Column(name = "ST_REGI", nullable = false)
    private String estado;
    
    @Column(name = "ID_USUA_CREA", nullable = false, length = 30)
    private String usuarioCreacion;

    @Column(name = "FE_USUA_CREA", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "DE_TERM_CREA", nullable = false, length = 30)
    private String terminalCreacion;

    @Column(name = "ID_USUA_MODI", length = 30, insertable = false)
    private String usuarioModificacion;

    @Column(name = "FE_USUA_MODI", insertable = false, updatable = false)
    private OffsetDateTime fechaModificacion;

    @Column(name = "DE_TERM_MODI", length = 30, insertable = false)
    private String terminalModificacion;
    
    @Column(name = "IN_VISI", nullable = true)
    private Integer realizaVisitas;
    
    @Column(name = "ID_CUES", nullable = true)
    private Integer idCuestionario;
    

	public Equipo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public User getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(User supervisor) {
		this.supervisor = supervisor;
	}

	public String getUsuarioCreacion() {
		return usuarioCreacion;
	}

	public void setUsuarioCreacion(String usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getTerminalCreacion() {
		return terminalCreacion;
	}

	public void setTerminalCreacion(String terminalCreacion) {
		this.terminalCreacion = terminalCreacion;
	}

	public String getUsuarioModificacion() {
		return usuarioModificacion;
	}

	public void setUsuarioModificacion(String usuarioModificacion) {
		this.usuarioModificacion = usuarioModificacion;
	}

	public OffsetDateTime getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(OffsetDateTime fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public String getTerminalModificacion() {
		return terminalModificacion;
	}

	public void setTerminalModificacion(String terminalModificacion) {
		this.terminalModificacion = terminalModificacion;
	}

	public Integer getRealizaVisitas() {
		return realizaVisitas;
	}

	public void setRealizaVisitas(Integer realizaVisitas) {
		this.realizaVisitas = realizaVisitas;
	}

	public Integer getIdCuestionario() {
		return idCuestionario;
	}

	public void setIdCuestionario(Integer idCuestionario) {
		this.idCuestionario = idCuestionario;
	}
    
	
    
}
