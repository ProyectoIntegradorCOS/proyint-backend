package pe.gob.onp.thaqhiri.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class HorarioDTO {

    private Long id;
    private String nombre;
    private Integer horaInicio;
    private Integer horaFin;
    private Integer estado;
    private String idUsuaCrea;
    private Date feUsuaCrea;
    private String deTermCrea;
    private String idUsuaModi;
    private LocalDateTime feUsuaModi;
    private String deTermModi;
    
    // Getters y Setters
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Integer getHoraInicio() {
		return horaInicio;
	}
	public void setHoraInicio(Integer horaInicio) {
		this.horaInicio = horaInicio;
	}
	public Integer getHoraFin() {
		return horaFin;
	}
	public void setHoraFin(Integer horaFin) {
		this.horaFin = horaFin;
	}
	public Integer getEstado() {
		return estado;
	}
	public void setEstado(Integer estado) {
		this.estado = estado;
	}
	
	
	
	public String getIdUsuaCrea() {
		return idUsuaCrea;
	}
	public void setIdUsuaCrea(String idUsuaCrea) {
		this.idUsuaCrea = idUsuaCrea;
	}
	public Date getFeUsuaCrea() {
		return feUsuaCrea;
	}
	public void setFeUsuaCrea(Date feUsuaCrea) {
		this.feUsuaCrea = feUsuaCrea;
	}
	public String getDeTermCrea() {
		return deTermCrea;
	}
	public void setDeTermCrea(String deTermCrea) {
		this.deTermCrea = deTermCrea;
	}
	public String getIdUsuaModi() {
		return idUsuaModi;
	}
	public void setIdUsuaModi(String idUsuaModi) {
		this.idUsuaModi = idUsuaModi;
	}
	public LocalDateTime getFeUsuaModi() {
		return feUsuaModi;
	}
	public void setFeUsuaModi(LocalDateTime feUsuaModi) {
		this.feUsuaModi = feUsuaModi;
	}
	public String getDeTermModi() {
		return deTermModi;
	}
	public void setDeTermModi(String deTermModi) {
		this.deTermModi = deTermModi;
	}
	public HorarioDTO() {
		super();
		// TODO Auto-generated constructor stub
	}    
    
}
