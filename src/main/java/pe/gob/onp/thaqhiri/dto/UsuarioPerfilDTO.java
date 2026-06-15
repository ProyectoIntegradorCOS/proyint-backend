package pe.gob.onp.thaqhiri.dto;

import java.util.List;

public class UsuarioPerfilDTO {

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

    private List<Object> listaFiltroRol;
    private List<Object> listaFiltroUsuario;

    private PerfilDTO bcomponentePerfil;

    private String dominiRed;
    
    private String tipo;
    private int idEquipo;
    private int idHorario;
    private String nombrePerfil;

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

	public List<Object> getListaFiltroRol() {
		return listaFiltroRol;
	}

	public void setListaFiltroRol(List<Object> listaFiltroRol) {
		this.listaFiltroRol = listaFiltroRol;
	}

	public List<Object> getListaFiltroUsuario() {
		return listaFiltroUsuario;
	}

	public void setListaFiltroUsuario(List<Object> listaFiltroUsuario) {
		this.listaFiltroUsuario = listaFiltroUsuario;
	}

	public PerfilDTO getBcomponentePerfil() {
		return bcomponentePerfil;
	}

	public void setBcomponentePerfil(PerfilDTO bcomponentePerfil) {
		this.bcomponentePerfil = bcomponentePerfil;
	}

	public String getDominiRed() {
		return dominiRed;
	}

	public void setDominiRed(String dominiRed) {
		this.dominiRed = dominiRed;
	}

	public UsuarioPerfilDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getIdEquipo() {
		return idEquipo;
	}

	public void setIdEquipo(int idEquipo) {
		this.idEquipo = idEquipo;
	}

	public int getIdHorario() {
		return idHorario;
	}

	public void setIdHorario(int idHorario) {
		this.idHorario = idHorario;
	}

	public String getNombrePerfil() {
		return nombrePerfil;
	}

	public void setNombrePerfil(String nombrePerfil) {
		this.nombrePerfil = nombrePerfil;
	}
    
	
    
}
