package pe.gob.onp.thaqhiri.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "import_job")
public class ImportJob {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "import_job_seq")
    @SequenceGenerator(name = "import_job_seq", sequenceName = "seq_impo_job", allocationSize = 1)
    @Column(name = "id_impo")
	private Long id;

    @Column(name = "nu_tota_fila", nullable = false)
    private int totalFilas;

    @Column(name = "nu_fila_proc", nullable = false)
    private int filasProcesadas;

    @Column(name = "nu_porc", nullable = false)
    private int porcentaje;

    @Column(name = "de_esta", nullable = false, length = 20)
    private String estado; // PROCESANDO, COMPLETADO, ERROR

    @Column(name = "de_mens")
    private String mensaje;

    @Column(name = "fe_inic", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fe_fin")
    private LocalDateTime fechaFin;
    
    @Column(name = "nu_hora_resta")
    private long nuHorasRestantes;
    
    @Column(name = "nu_minu_resta")
    private long nuMinutosRestantes;
    
    @Column(name = "nu_segu_resta")
    private long nuSegundosRestantes;
    
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
    

	public ImportJob() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getTotalFilas() {
		return totalFilas;
	}

	public void setTotalFilas(int totalFilas) {
		this.totalFilas = totalFilas;
	}

	public int getFilasProcesadas() {
		return filasProcesadas;
	}

	public void setFilasProcesadas(int filasProcesadas) {
		this.filasProcesadas = filasProcesadas;
	}

	public int getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(int porcentaje) {
		this.porcentaje = porcentaje;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDateTime fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDateTime fechaFin) {
		this.fechaFin = fechaFin;
	}

	public long getNuHorasRestantes() {
		return nuHorasRestantes;
	}

	public void setNuHorasRestantes(long nuHorasRestantes) {
		this.nuHorasRestantes = nuHorasRestantes;
	}

	public long getNuMinutosRestantes() {
		return nuMinutosRestantes;
	}

	public void setNuMinutosRestantes(long nuMinutosRestantes) {
		this.nuMinutosRestantes = nuMinutosRestantes;
	}

	public long getNuSegundosRestantes() {
		return nuSegundosRestantes;
	}

	public void setNuSegundosRestantes(long nuSegundosRestantes) {
		this.nuSegundosRestantes = nuSegundosRestantes;
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

    
    
}