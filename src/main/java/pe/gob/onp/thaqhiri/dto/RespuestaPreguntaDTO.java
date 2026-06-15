package pe.gob.onp.thaqhiri.dto;

import java.util.Date;

public class RespuestaPreguntaDTO {
	    private Long id;
	    private Long idPersona;
	    private Long idCuestionario;
	    private Long idPregunta;
	    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Incluye el item de visita para filtrar respuestas por visita][obj: RespuestaPreguntaDTO.idItem]
	    private Long idItem;
	    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Incluye el plan de visitas para trazabilidad de respuestas][obj: RespuestaPreguntaDTO.idPlan]
	    private Long idPlan;
	    private String textoPregunta;
	    private String respuesta;
	    private Integer estado;
	    
		public RespuestaPreguntaDTO() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getIdPersona() {
			return idPersona;
		}

		public void setIdPersona(Long idPersona) {
			this.idPersona = idPersona;
		}

		public Long getIdPregunta() {
			return idPregunta;
		}

		public void setIdPregunta(Long idPregunta) {
			this.idPregunta = idPregunta;
		}

		public Long getIdItem() {
			return idItem;
		}

		public void setIdItem(Long idItem) {
			this.idItem = idItem;
		}

		public Long getIdPlan() {
			return idPlan;
		}

		public void setIdPlan(Long idPlan) {
			this.idPlan = idPlan;
		}

		public String getRespuesta() {
			return respuesta;
		}

		public void setRespuesta(String respuesta) {
			this.respuesta = respuesta;
		}

		public Integer getEstado() {
			return estado;
		}

		public void setEstado(Integer estado) {
			this.estado = estado;
		}

		public String getTextoPregunta() {
			return textoPregunta;
		}

		public void setTextoPregunta(String textoPregunta) {
			this.textoPregunta = textoPregunta;
		}

		public Long getIdCuestionario() {
			return idCuestionario;
		}

		public void setIdCuestionario(Long idCuestionario) {
			this.idCuestionario = idCuestionario;
		}
		
			    
	    
}
