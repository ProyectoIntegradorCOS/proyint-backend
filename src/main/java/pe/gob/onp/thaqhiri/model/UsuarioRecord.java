package pe.gob.onp.thaqhiri.model;

public class UsuarioRecord {
	
	private String idUsuario;
	private String usuario;
	
	
	
	public UsuarioRecord(String idUsuario, String usuario) {
		super();
		this.idUsuario = idUsuario;
		this.usuario = usuario;
	}

	public UsuarioRecord() {
		super();
	}

	public String getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(String idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	

}
