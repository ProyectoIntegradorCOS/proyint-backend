package pe.gob.onp.thaqhiri.model;

public class UbicacionRecord {
	private Long idUbicacion;
    private Integer idPersona;
    private String nombrePersona;
    private String usuario;
    private String fecha;
    private String hora;
    private Double lon;
    private Double lat;
    private int correlativo;

    public UbicacionRecord() {}

    public UbicacionRecord(Long idUbicacion, Integer idPersona, String nombrePersona, String usuario, String fecha, String hora, Double lon, Double lat, int correlativo) {
    	this.idUbicacion = idUbicacion;
        this.idPersona = idPersona;
        this.nombrePersona = nombrePersona;
        this.fecha = fecha;
        this.lon = lon;
        this.lat = lat;
        this.usuario = usuario;
        this.hora = hora;
        this.correlativo = correlativo;
    }

    // getters y setters
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

	public Integer getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(Integer idPersona) {
		this.idPersona = idPersona;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getHora() {
		return hora;
	}

	public void setHora(String hora) {
		this.hora = hora;
	}

	public int getCorrelativo() {
		return correlativo;
	}

	public void setCorrelativo(int correlativo) {
		this.correlativo = correlativo;
	}

	public String getNombrePersona() {
		return nombrePersona;
	}

	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}

	public Long getIdUbicacion() {
		return idUbicacion;
	}

	public void setIdUbicacion(Long idUbicacion) {
		this.idUbicacion = idUbicacion;
	}
	
	
    
}
