package pe.gob.onp.thaqhiri.dto;

import java.util.List;

import pe.gob.onp.thaqhiri.model.ResultadoValidacion;
import pe.gob.onp.thaqhiri.model.VisitItemMasivo;

public class VisitPlanValidaResultDTO {
	
	ResultadoValidacion resultadoValidacion;
	
	List<VisitItemMasivo> listaItemsExcel;

	public VisitPlanValidaResultDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResultadoValidacion getResultadoValidacion() {
		return resultadoValidacion;
	}

	public void setResultadoValidacion(ResultadoValidacion resultadoValidacion) {
		this.resultadoValidacion = resultadoValidacion;
	}

	public List<VisitItemMasivo> getListaItemsExcel() {
		return listaItemsExcel;
	}

	public void setListaItemsExcel(List<VisitItemMasivo> listaItemsExcel) {
		this.listaItemsExcel = listaItemsExcel;
	}

	
}
