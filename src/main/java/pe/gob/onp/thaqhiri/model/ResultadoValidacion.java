package pe.gob.onp.thaqhiri.model;

public class ResultadoValidacion {
	
	private TipoResultadoValidacion tipoResultado;
	private String mensaje;
	
	public ResultadoValidacion() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TipoResultadoValidacion getTipoResultado() {
		return tipoResultado;
	}

	public void setTipoResultado(TipoResultadoValidacion resultado) {
		this.tipoResultado = resultado;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	} 
	
	

}
