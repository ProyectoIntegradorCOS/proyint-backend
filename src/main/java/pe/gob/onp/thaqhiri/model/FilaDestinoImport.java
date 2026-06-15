package pe.gob.onp.thaqhiri.model;

import java.time.LocalDate;

public class FilaDestinoImport {

    private String nombre;
    private String direccion;
    private LocalDate fechaActualizacion;
    private String ubicabilidad;
    private String estadoOnp;
    
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public LocalDate getFechaActualizacion() {
		return fechaActualizacion;
	}
	public void setFechaActualizacion(LocalDate fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}
	public String getUbicabilidad() {
		return ubicabilidad;
	}
	public void setUbicabilidad(String ubicabilidad) {
		this.ubicabilidad = ubicabilidad;
	}
	public String getEstadoOnp() {
		return estadoOnp;
	}
	public void setEstadoOnp(String estadoOnp) {
		this.estadoOnp = estadoOnp;
	}
	
	public FilaDestinoImport() {
		super();
		// TODO Auto-generated constructor stub
	}    

}