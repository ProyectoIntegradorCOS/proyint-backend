package pe.gob.onp.thaqhiri.dto;

/**
 * DTO para transferir datos de Equipo. 
 * Contiene solo los campos necesarios para la lista desplegable del frontend.
 */
public class EquipoDTO {
    private Integer id;
    private String nombre;
    private Long supervisorId;
    private String supervisorNombre;
    private String usuarioSesion; 
    private String terminalSesion;
    private Boolean realizaVisitas;
    private Integer idCuestionario;
    private String cuestionarioNombre; 

    public EquipoDTO() {
        // Constructor vacío necesario para mapeo
    }

    public EquipoDTO(Integer id, String nombre, Long supervisorId, String supervisorNombre, Boolean realizaVisitas, Integer idCuestionario, String cuestionarioNombre) {
        this.id = id;
        this.nombre = nombre;
        this.supervisorId = supervisorId;
        this.supervisorNombre = supervisorNombre;
        this.realizaVisitas = realizaVisitas;
        this.idCuestionario = idCuestionario;
        this.cuestionarioNombre = cuestionarioNombre;
    }
    
    // --- Getters y Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

	public Long getSupervisorId() {
		return supervisorId;
	}

	public void setSupervisorId(Long supervisorId) {
		this.supervisorId = supervisorId;
	}

	public String getSupervisorNombre() {
		return supervisorNombre;
	}

	public void setSupervisorNombre(String supervisorNombre) {
		this.supervisorNombre = supervisorNombre;
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

	public Boolean getRealizaVisitas() {
		return realizaVisitas;
	}

	public void setRealizaVisitas(Boolean realizaVisitas) {
		this.realizaVisitas = realizaVisitas;
	}

	public Integer getIdCuestionario() {
		return idCuestionario;
	}

	public void setIdCuestionario(Integer idCuestionario) {
		this.idCuestionario = idCuestionario;
	}

	public String getCuestionarioNombre() {
		return cuestionarioNombre;
	}

	public void setCuestionarioNombre(String cuestionarioNombre) {
		this.cuestionarioNombre = cuestionarioNombre;
	}
    
	
    
}