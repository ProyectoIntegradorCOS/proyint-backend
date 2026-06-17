package pe.gob.onp.thaqhiri.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import pe.gob.onp.thaqhiri.dto.RespuestaPreguntaDTO;
import pe.gob.onp.thaqhiri.entity.Respuesta;
import pe.gob.onp.thaqhiri.repository.PreguntaRepository;
import pe.gob.onp.thaqhiri.repository.RespuestaRepository;
import pe.gob.onp.thaqhiri.repository.VisitItemRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import java.util.List;
import java.util.Optional;
import pe.gob.onp.thaqhiri.entity.Pregunta;
import pe.gob.onp.thaqhiri.entity.VisitItem;

@Service
public class RespuestaService {

    @Autowired
    private RespuestaRepository respuestaRepository;
    @Autowired
    private PreguntaRepository preguntaRepository;
    @Autowired
    private VisitItemRepository visitItemRepository;

    // Buscar con paginación
    public RespuestaPreguntaDTO buscar(Long idPregunta){

    	Respuesta resultado = null;
    	
        // Obtenemos la página de entidades
    	Optional<Respuesta> oRespuesta = respuestaRepository.findByIdPreguntaAndEstado(idPregunta, UConstante.ACTIVO_REGI);

    	if(oRespuesta.isPresent()) {
    		resultado = oRespuesta.get(); 
    	}
    	
        // Convertimos cada entidad a DTO usando el método toDTO
    	RespuestaPreguntaDTO resultadoDto = toDTO(resultado);

        return resultadoDto;
    }

    // Crear respuesta
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-18 09:00 UTC-5 (Lima)][desc: Idempotencia: upsert por (idPregunta, idItem) para evitar duplicados en reintentos offline][obj: RespuestaService.crear]
    public RespuestaPreguntaDTO crear(RespuestaPreguntaDTO dto, String usuario, String terminal){
        Optional<Respuesta> existing = respuestaRepository
                .findFirstByIdPreguntaAndIdItemAndEstadoOrderByIdDesc(dto.getIdPregunta(), dto.getIdItem(), UConstante.ACTIVO_REGI);
        Respuesta r = existing.orElseGet(Respuesta::new);
        r.setIdPersona(dto.getIdPersona());
        r.setIdCuestionario(dto.getIdCuestionario());
        r.setIdPregunta(dto.getIdPregunta());
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Asocia la respuesta con el item de visita][obj: RespuestaService.crear]
        r.setIdItem(dto.getIdItem());
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Deriva el plan desde el item de visita][obj: RespuestaService.crear idPlan]
        r.setIdPlan(resolvePlanId(dto.getIdItem()));
        r.setPregunta(dto.getTextoPregunta());
        r.setRespuesta(dto.getRespuesta());
        r.setEstado(UConstante.ACTIVO_REGI);
        if (existing.isEmpty()) {
            r.setUsuarioCrea(usuario);
            r.setTerminalCrea(terminal);
        } else {
            r.setUsuarioModi(usuario);
            r.setTerminalModi(terminal);
        }
        return toDTO(respuestaRepository.save(r));
    }

    // Actualizar respuesta
    public RespuestaPreguntaDTO actualizar(Long id, RespuestaPreguntaDTO dto, String usuario, String terminal){
        Optional<Respuesta> optional = respuestaRepository.findById(id);
        if(optional.isPresent()){
            Respuesta r = optional.get();
            r.setIdPersona(dto.getIdPersona());
            r.setIdCuestionario(dto.getIdCuestionario());
            r.setIdPregunta(dto.getIdPregunta());
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Mantiene la asociacion de item de visita al actualizar][obj: RespuestaService.actualizar]
            r.setIdItem(dto.getIdItem());
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Deriva el plan desde el item de visita en actualizacion][obj: RespuestaService.actualizar idPlan]
            r.setIdPlan(resolvePlanId(dto.getIdItem()));
            r.setPregunta(dto.getTextoPregunta());
            r.setRespuesta(dto.getRespuesta());
            r.setEstado(UConstante.ACTIVO_REGI);
            r.setUsuarioModi(usuario);
            r.setTerminalModi(terminal);
            return toDTO(respuestaRepository.save(r));
        }
        throw new RuntimeException("Respuesta no encontrada");
    }

    // Eliminar respuesta
    public void eliminar(Long id, String usuario, String terminal){
    	respuestaRepository.desactivar(id, usuario, terminal);
    }
    
    public RespuestaPreguntaDTO toDTO(Respuesta entidad) {
    	
    	RespuestaPreguntaDTO dto = new RespuestaPreguntaDTO();
    	
    	dto.setEstado(entidad.getEstado());
    	dto.setId(entidad.getId());
    	dto.setIdPersona(entidad.getIdPersona());
    	dto.setIdCuestionario(entidad.getIdCuestionario());
    	// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Propaga el item de visita en el DTO][obj: RespuestaService.toDTO]
    	dto.setIdItem(entidad.getIdItem());
    	// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Propaga el plan de visitas en el DTO][obj: RespuestaService.toDTO]
    	dto.setIdPlan(entidad.getIdPlan());
    	dto.setTextoPregunta(entidad.getPregunta());
    	dto.setIdPregunta(entidad.getIdPregunta());
    	dto.setRespuesta(entidad.getRespuesta());
    	
    	Pregunta p = preguntaRepository.findById(entidad.getIdPregunta()).orElseThrow();    	
    	dto.setTextoPregunta(p.getDescripcion());
    	
    	return dto;
    }
    
    
    public List<RespuestaPreguntaDTO> obtenerRespuestasPorCuestionario(Long idCuestionario, Long idPersona) {
        // 1. Obtenemos todas las respuestas del usuario para el cuestionario, ordenadas por id
        List<Respuesta> respuestas = respuestaRepository.findByPersonaAndCuestionarioOrdenadas(idPersona, idCuestionario);

        // 2. Mapear a DTO
        return respuestas.stream().map(r -> {
            Pregunta p = preguntaRepository.findById(r.getIdPregunta())
                    .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));
            
            RespuestaPreguntaDTO dto = new RespuestaPreguntaDTO();
            dto.setIdPregunta(p.getId());
            dto.setTextoPregunta(p.getDescripcion());
            dto.setRespuesta(r.getRespuesta());
            dto.setId(r.getId());
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Incluye item de visita al listar por cuestionario][obj: RespuestaService.obtenerRespuestasPorCuestionario]
            dto.setIdItem(r.getIdItem());
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Incluye plan de visitas al listar por cuestionario][obj: RespuestaService.obtenerRespuestasPorCuestionario]
            dto.setIdPlan(r.getIdPlan());
            return dto;
        }).toList();
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:23 UTC-5 (Lima)][desc: Obtiene respuestas asociadas a un item de visita][obj: RespuestaService.obtenerRespuestasPorItem]
    public List<RespuestaPreguntaDTO> obtenerRespuestasPorItem(Long idItem) {
        List<Respuesta> respuestas = respuestaRepository.findByItemOrdenadas(idItem, UConstante.ACTIVO_REGI);

        return respuestas.stream().map(r -> {
            Pregunta p = preguntaRepository.findById(r.getIdPregunta())
                    .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));

            RespuestaPreguntaDTO dto = new RespuestaPreguntaDTO();
            dto.setIdPregunta(p.getId());
            dto.setTextoPregunta(p.getDescripcion());
            dto.setRespuesta(r.getRespuesta());
            dto.setId(r.getId());
            dto.setIdItem(r.getIdItem());
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Incluye plan de visitas al listar por item][obj: RespuestaService.obtenerRespuestasPorItem]
            dto.setIdPlan(r.getIdPlan());
            return dto;
        }).toList();
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-22 09:31 UTC-5 (Lima)][desc: Deriva el plan desde el item para asegurar trazabilidad][obj: RespuestaService.resolvePlanId]
    private Long resolvePlanId(Long idItem) {
        if (idItem == null) {
            return null;
        }
        VisitItem item = visitItemRepository.findById(idItem)
                .orElseThrow(() -> new RuntimeException("Item de visita no encontrado"));
        return item.getPlan().getId();
    }

    
}
