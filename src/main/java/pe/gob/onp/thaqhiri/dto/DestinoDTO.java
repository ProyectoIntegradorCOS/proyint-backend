package pe.gob.onp.thaqhiri.dto;

import java.time.LocalDate;

public class DestinoDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private String direccion;
    private String departamento;
    private String provincia;
    private String distrito;
    private Double latitud;
    private Double longitud;
    private String referencia;
    private String zona;
    private String horarios;
    private String contacto;
    private String precision; // CONFIRMADO / APROXIMADO
    private Boolean activo;
    private String ubicabilidadOnp;
    private String estadoOnp;
    private LocalDate fechaActualizacionOnp;
    
    /* Indica si el destino está validado, en caso de haber sido registrado recien en la carga masiva. */
    private boolean validado;
    private String mensajeValidacion;

    private String usuarioSesion;
    private String terminalSesion;

    public DestinoDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getHorarios() {
        return horarios;
    }

    public void setHorarios(String horarios) {
        this.horarios = horarios;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getUsuarioSesion() {
        return usuarioSesion;
    }

    public void setUsuarioSesion(String usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }

    public String getTerminalSesion() {
        return terminalSesion;
    }

    public void setTerminalSesion(String terminalSesion) {
        this.terminalSesion = terminalSesion;
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

	public String getEstadoOnp() {
		return estadoOnp;
	}

	public void setEstadoOnp(String estadoOnp) {
		this.estadoOnp = estadoOnp;
	}

	public String getUbicabilidadOnp() {
		return ubicabilidadOnp;
	}

	public void setUbicabilidadOnp(String ubicabilidadOnp) {
		this.ubicabilidadOnp = ubicabilidadOnp;
	}

	public LocalDate getFechaActualizacionOnp() {
		return fechaActualizacionOnp;
	}

	public void setFechaActualizacionOnp(LocalDate fechaActualizacionOnp) {
		this.fechaActualizacionOnp = fechaActualizacionOnp;
	}
	
	

    
}
