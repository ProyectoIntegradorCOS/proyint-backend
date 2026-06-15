package pe.gob.onp.thaqhiri.dto;

import java.util.Date;
import java.util.List;

public class PreguntaDTO {
    private Long id;
    private Long idCuestionario;
    private String descripcion;
    private String tipo;
    private String obligatorio;
    private Integer orden;
    private String grupo;
    private Integer estado;
    private Long idSiguientePregunta;
    private List<OpcionDTO> opciones;
    
	public PreguntaDTO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdCuestionario() {
		return idCuestionario;
	}

	public void setIdCuestionario(Long idCuestionario) {
		this.idCuestionario = idCuestionario;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getObligatorio() {
		return obligatorio;
	}

	public void setObligatorio(String obligatorio) {
		this.obligatorio = obligatorio;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public List<OpcionDTO> getOpciones() {
		return opciones;
	}

	public void setOpciones(List<OpcionDTO> opciones) {
		this.opciones = opciones;
	}

	public Long getIdSiguientePregunta() {
		return idSiguientePregunta;
	}

	public void setIdSiguientePregunta(Long idSiguientePregunta) {
		this.idSiguientePregunta = idSiguientePregunta;
	}
	
	

    
}
