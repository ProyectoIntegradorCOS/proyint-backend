package pe.gob.onp.thaqhiri.dto;

import pe.gob.onp.thaqhiri.model.VisitPlanStatus;

public class ReporteProductividadDTO {

    private Long idVerifier;
    private String nombreVerifier;
    private String equipo;

    private Long totalVisitas;
    private Long completadas;
    private Long terminadas;
    private Long pendientes;

    private String fechaPlan;    
    private Long idPlan;
    private VisitPlanStatus estadoPlan;
    
	public Long getIdVerifier() {
		return idVerifier;
	}
	public void setIdVerifier(Long idVerifier) {
		this.idVerifier = idVerifier;
	}
	public String getNombreVerifier() {
		return nombreVerifier;
	}
	public void setNombreVerifier(String nombreVerifier) {
		this.nombreVerifier = nombreVerifier;
	}
	public String getEquipo() {
		return equipo;
	}
	public void setEquipo(String equipo) {
		this.equipo = equipo;
	}

	public Long getTotalVisitas() {
		return totalVisitas;
	}
	public void setTotalVisitas(Long totalVisitas) {
		this.totalVisitas = totalVisitas;
	}
	public Long getCompletadas() {
		return completadas;
	}
	public void setCompletadas(Long completadas) {
		this.completadas = completadas;
	}
	public Long getPendientes() {
		return pendientes;
	}
	public void setPendientes(Long pendientes) {
		this.pendientes = pendientes;
	}
	public String getFechaPlan() {
		return fechaPlan;
	}
	public void setFechaPlan(String fechaPlan) {
		this.fechaPlan = fechaPlan;
	}
	public Long getIdPlan() {
		return idPlan;
	}
	public void setIdPlan(Long idPlan) {
		this.idPlan = idPlan;
	}
	
	
	public VisitPlanStatus getEstadoPlan() {
		return estadoPlan;
	}
	public void setEstadoPlan(VisitPlanStatus estadoPlan) {
		this.estadoPlan = estadoPlan;
	}
	
	
	
	public Long getTerminadas() {
		return terminadas;
	}
	public void setTerminadas(Long terminadas) {
		this.terminadas = terminadas;
	}
	
	public ReporteProductividadDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ReporteProductividadDTO(Long idVerifier, String nombreVerifier, String equipo, 
			Long totalVisitas, Long completadas, Long terminadas, Long pendientes, String fechaPlan,
			Long idPlan, VisitPlanStatus estadoPlan) 
	{		
		super();
		
		this.idVerifier = idVerifier;
		this.nombreVerifier = nombreVerifier;
		this.equipo = equipo;
		this.totalVisitas = totalVisitas;
		this.completadas = completadas;
		this.terminadas = terminadas;
		this.pendientes = pendientes;
		this.fechaPlan = fechaPlan;
		this.idPlan = idPlan;
		this.estadoPlan = estadoPlan;
	}
    
	
    
}