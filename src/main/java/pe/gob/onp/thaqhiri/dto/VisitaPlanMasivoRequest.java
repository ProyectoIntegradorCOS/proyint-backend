package pe.gob.onp.thaqhiri.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import pe.gob.onp.thaqhiri.model.VisitItemMasivo;

public class VisitaPlanMasivoRequest {

    private String usuarioSesion;

    @NotEmpty
    @Valid
    private List<VisitItemMasivo> visitas;

	public VisitaPlanMasivoRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getUsuarioSesion() {
		return usuarioSesion;
	}

	public void setUsuarioSesion(String usuarioSesion) {
		this.usuarioSesion = usuarioSesion;
	}

	public List<VisitItemMasivo> getVisitas() {
		return visitas;
	}

	public void setVisitas(List<VisitItemMasivo> visitas) {
		this.visitas = visitas;
	}
    
    
    
}
