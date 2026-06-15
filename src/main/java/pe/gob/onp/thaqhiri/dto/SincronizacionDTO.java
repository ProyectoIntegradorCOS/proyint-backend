package pe.gob.onp.thaqhiri.dto;

import java.util.List;

public class SincronizacionDTO {

	private String codigoResultado;
    private String mensajeResultado;
    
	List<UsuarioPerfilDTO> listaUsuariosNuevosSAA;
	List<UsuarioPerfilDTO> listaUsuariosSAALocalesActivar;
	
	public SincronizacionDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getCodigoResultado() {
		return codigoResultado;
	}

	public void setCodigoResultado(String codigoResultado) {
		this.codigoResultado = codigoResultado;
	}

	public String getMensajeResultado() {
		return mensajeResultado;
	}

	public void setMensajeResultado(String mensajeResultado) {
		this.mensajeResultado = mensajeResultado;
	}

	public List<UsuarioPerfilDTO> getListaUsuariosNuevosSAA() {
		return listaUsuariosNuevosSAA;
	}

	public void setListaUsuariosNuevosSAA(List<UsuarioPerfilDTO> listaUsuariosNuevosSAA) {
		this.listaUsuariosNuevosSAA = listaUsuariosNuevosSAA;
	}

	public List<UsuarioPerfilDTO> getListaUsuariosSAALocalesActivar() {
		return listaUsuariosSAALocalesActivar;
	}

	public void setListaUsuariosSAALocalesActivar(List<UsuarioPerfilDTO> listaUsuariosSAALocalesActivar) {
		this.listaUsuariosSAALocalesActivar = listaUsuariosSAALocalesActivar;
	}
	
	
	
}
