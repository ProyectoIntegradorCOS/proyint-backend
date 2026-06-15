package pe.gob.onp.thaqhiri.dto;

import java.util.List;

public class UsuarioSaaDTO {
    private String login;
    private String tipoDoc;
    private String docIdentidad;
    private String nombres;
    private String apePaterno;
    private String apeMaterno;
    private String firmaDigitalizada;
    private int idUsuario;
    private int idInstitucion;
    private int idRol;
    private int idServicio;
    private List<?> listaFiltroRol;
    private List<?> listaFiltroUsuario;
    private BComponentePerfil bcomponentePerfil;
    
    

    public UsuarioSaaDTO() {
		super();
		// TODO Auto-generated constructor stub
	}



	public String getLogin() {
		return login;
	}



	public void setLogin(String login) {
		this.login = login;
	}



	public String getTipoDoc() {
		return tipoDoc;
	}



	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}



	public String getDocIdentidad() {
		return docIdentidad;
	}



	public void setDocIdentidad(String docIdentidad) {
		this.docIdentidad = docIdentidad;
	}



	public String getNombres() {
		return nombres;
	}



	public void setNombres(String nombres) {
		this.nombres = nombres;
	}



	public String getApePaterno() {
		return apePaterno;
	}



	public void setApePaterno(String apePaterno) {
		this.apePaterno = apePaterno;
	}



	public String getApeMaterno() {
		return apeMaterno;
	}



	public void setApeMaterno(String apeMaterno) {
		this.apeMaterno = apeMaterno;
	}



	public String getFirmaDigitalizada() {
		return firmaDigitalizada;
	}



	public void setFirmaDigitalizada(String firmaDigitalizada) {
		this.firmaDigitalizada = firmaDigitalizada;
	}



	public int getIdUsuario() {
		return idUsuario;
	}



	public void setIdUsuario(int idUsuario) {
		this.idUsuario = idUsuario;
	}



	public int getIdInstitucion() {
		return idInstitucion;
	}



	public void setIdInstitucion(int idInstitucion) {
		this.idInstitucion = idInstitucion;
	}



	public int getIdRol() {
		return idRol;
	}



	public void setIdRol(int idRol) {
		this.idRol = idRol;
	}



	public int getIdServicio() {
		return idServicio;
	}



	public void setIdServicio(int idServicio) {
		this.idServicio = idServicio;
	}



	public List<?> getListaFiltroRol() {
		return listaFiltroRol;
	}



	public void setListaFiltroRol(List<?> listaFiltroRol) {
		this.listaFiltroRol = listaFiltroRol;
	}



	public List<?> getListaFiltroUsuario() {
		return listaFiltroUsuario;
	}



	public void setListaFiltroUsuario(List<?> listaFiltroUsuario) {
		this.listaFiltroUsuario = listaFiltroUsuario;
	}



	public BComponentePerfil getBcomponentePerfil() {
		return bcomponentePerfil;
	}



	public void setBcomponentePerfil(BComponentePerfil bcomponentePerfil) {
		this.bcomponentePerfil = bcomponentePerfil;
	}



	public static class BComponentePerfil {
        private int idPerfil;
        private int idSistema;
        
		public int getIdPerfil() {
			return idPerfil;
		}
		public void setIdPerfil(int idPerfil) {
			this.idPerfil = idPerfil;
		}
		public int getIdSistema() {
			return idSistema;
		}
		public void setIdSistema(int idSistema) {
			this.idSistema = idSistema;
		}
		
		public BComponentePerfil() {
			super();
			// TODO Auto-generated constructor stub
		}        
        
    }
}
