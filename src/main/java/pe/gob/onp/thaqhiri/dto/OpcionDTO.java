package pe.gob.onp.thaqhiri.dto;


public class OpcionDTO {

    private Long id;
    private Long idPregunta;
    private String descripcion;
    private String valor;
    private Integer orden;
    private Integer estado;
    private Long idSiguientePregunta;
    
	public OpcionDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdPregunta() {
		return idPregunta;
	}

	public void setIdPregunta(Long idPregunta) {
		this.idPregunta = idPregunta;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public Long getIdSiguientePregunta() {
		return idSiguientePregunta;
	}

	public void setIdSiguientePregunta(Long idSiguientePregunta) {
		this.idSiguientePregunta = idSiguientePregunta;
	}

	public Integer getOrden() {
		return orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}
        

    
    
}
