package pe.gob.onp.thaqhiri.model;

public class VisitItemMasivo {
	private Long colaboradorId;
	private String fecha;          // yyyy-MM-dd
    private String destinoNombre;
    private String direccion;
    private String horaCita;
    private String prioridad;
    private String plantillaPv;
    private Long destinoId;
    
    /* Indica si el destino ha sido validado o requiere valodacion por haber sido registrado como parte de la carga masiva */
    private boolean validado;
    private String mensajeValidacion;
    
	public VisitItemMasivo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getColaboradorId() {
		return colaboradorId;
	}

	public void setColaboradorId(Long colaboradorId) {
		this.colaboradorId = colaboradorId;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getDestinoNombre() {
		return destinoNombre;
	}

	public void setDestinoNombre(String destinoNombre) {
		this.destinoNombre = destinoNombre;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getHoraCita() {
		return horaCita;
	}

	public void setHoraCita(String horaCita) {
		this.horaCita = horaCita;
	}

	public String getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(String prioridad) {
		this.prioridad = prioridad;
	}

	public String getPlantillaPv() {
		return plantillaPv;
	}

	public void setPlantillaPv(String plantillaPv) {
		this.plantillaPv = plantillaPv;
	}

	public Long getDestinoId() {
		return destinoId;
	}

	public void setDestinoId(Long destinoId) {
		this.destinoId = destinoId;
	}

	public boolean isValidado() {
		return validado;
	}

	public void setValidado(boolean validado) {
		this.validado = validado;
	}

	public String getMensajeValidacion() {
		return mensajeValidacion;
	}

	public void setMensajeValidacion(String mensajeValidacion) {
		this.mensajeValidacion = mensajeValidacion;
	}

		
    
}
