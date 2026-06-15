package pe.gob.onp.thaqhiri.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import pe.gob.onp.thaqhiri.dto.OpcionDTO;
import pe.gob.onp.thaqhiri.dto.PreguntaDTO;
import pe.gob.onp.thaqhiri.entity.Opcion;
import pe.gob.onp.thaqhiri.entity.Pregunta;
import pe.gob.onp.thaqhiri.repository.OpcionRepository;
import pe.gob.onp.thaqhiri.repository.PreguntaRepository;
import pe.gob.onp.thaqhiri.util.UConstante;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PreguntaService {

    @Autowired
    private PreguntaRepository preguntaRepository;    
    
    @Autowired
    private OpcionService opcionService;
    
    @Autowired
    private OpcionRepository opcionRepository;
    
    

    // Buscar con paginación
    public List<PreguntaDTO> listarPorCuestionario(long idCuestionario) {
        
    	List<Pregunta> preguntas = preguntaRepository.findByIdCuestionarioAndEstadoOrderByOrden(idCuestionario, UConstante.ACTIVO_REGI);
        
        // Convertimos a DTO usando Stream y el método toDTO
        List<PreguntaDTO> resultado = preguntas.stream()
                                               .map(this::toDTO)
                                               .toList();
        
        
        return resultado;
        
    }

    
    // Crear nueva pregunta
    @Transactional
    public PreguntaDTO crear(PreguntaDTO dto, String usuario, String terminal){
    	
    	int maxOrden = preguntaRepository.findMaxOrdenByCuestionario(dto.getIdCuestionario());    	
    	
        Pregunta p = new Pregunta();
        
        p.setIdCuestionario(dto.getIdCuestionario());
        p.setDescripcion(dto.getDescripcion());
        p.setTipo(dto.getTipo());
        p.setObligatorio(dto.getObligatorio());
        p.setOrden(maxOrden + 1);
        p.setGrupo(dto.getGrupo());
        p.setEstado(UConstante.ACTIVO_REGI);
        p.setUsuarioCrea(usuario);
        p.setTerminalCrea(terminal);
        p.setIdSiguientePregunta(dto.getIdSiguientePregunta());
        
        // Primero guardar solo la pregunta
        Pregunta entidadPregunta = preguntaRepository.save(p);
        
        // Luego guardar las opciones si es opción múltiple
        if ("O".equals(dto.getTipo()) && dto.getOpciones() != null) {
        	int nuOrden = 1;
        	
            for (OpcionDTO opDto : dto.getOpciones()) {            	
            	
            	opDto.setEstado(UConstante.ACTIVO);
            	opDto.setOrden(nuOrden++);
            	//Asigna el id de la pregunta recien creada
            	opDto.setIdPregunta(entidadPregunta.getId());
            	
            	//Registra la opcion
            	opcionService.crear(opDto, usuario, terminal);            	
            }            
        }
        
        // Despues convertirla a DTO, ese metodo ya obtiene las opciones de la pregunta        
        return toDTO(entidadPregunta);
    }
    
    
    // Actualizar pregunta
    @Transactional
    public PreguntaDTO actualizar(Long id, PreguntaDTO dto, String usuario, String terminal) {

        Pregunta p = preguntaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));

        p.setDescripcion(dto.getDescripcion());
        p.setTipo(dto.getTipo());
        p.setObligatorio(dto.getObligatorio());
        p.setOrden(dto.getOrden());
        p.setGrupo(dto.getGrupo());
        p.setEstado(UConstante.ACTIVO_REGI);
        p.setUsuarioModi(usuario);
        p.setTerminalModi(terminal);
        p.setIdSiguientePregunta(dto.getIdSiguientePregunta());

        preguntaRepository.save(p);

        // Sincronizar opciones si es múltiple
        if ("O".equals(dto.getTipo())) {
            opcionService.sincronizarOpciones(p.getId(), dto.getOpciones(), usuario, terminal);
        } else {
            // Si cambió de tipo, eliminar opciones
        	opcionService.desactivarPorPregunta(p.getId(), usuario, terminal);
        }

        return toDTO(p);
    }


    // Eliminar pregunta
    public void eliminar(Long id, String usuario, String terminal) {
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 11:44 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: PreguntaService.eliminar]
        opcionService.desactivarPorPregunta(id, usuario, terminal);
    	preguntaRepository.desactivar(id, usuario, terminal);
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 11:45 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: PreguntaService.desactivarPorCuestionario]
    public void desactivarPorCuestionario(Long idCuestionario, String usuario, String terminal) {
        List<Pregunta> preguntas = preguntaRepository.findByIdCuestionarioAndEstadoOrderByOrden(
                idCuestionario,
                UConstante.ACTIVO_REGI
        );
        for (Pregunta pregunta : preguntas) {
            opcionService.desactivarPorPregunta(pregunta.getId(), usuario, terminal);
        }
        preguntaRepository.desactivarPorCuestionario(idCuestionario, usuario, terminal);
    }
    
    
    public PreguntaDTO obtenerSiguientePregunta(Long idOpcionSeleccionada) {
        // Buscamos la opción
        Opcion opcion = opcionRepository.findById(idOpcionSeleccionada)
                .orElseThrow(() -> new RuntimeException("Opción no encontrada"));

        // Revisamos si hay siguiente pregunta
        if (opcion.getIdSiguientePregunta() == null) return null; // fin del flujo

        // Obtenemos la siguiente pregunta
        Pregunta siguiente = preguntaRepository.findById(opcion.getIdSiguientePregunta())
                .orElseThrow(() -> new RuntimeException("Pregunta siguiente no encontrada"));

        return toDTO(siguiente);
    }
    
    @Transactional
    public void actualizarOrden(List<PreguntaDTO> lista, String usuario, String terminal) {
    	
        for (PreguntaDTO dto : lista) {
            Pregunta p = preguntaRepository.findById(dto.getId()).orElseThrow();
            
            //JPA guarda automáticamente en batch los campos modificados cuando la ejecucion de este metodo termina,
            //ello por haber usado findById y porque el metodo es transaccional.            
            p.setOrden(dto.getOrden());
            p.setUsuarioModi(usuario);
            p.setTerminalModi(terminal);
            
            //Por eso, acá no es necesario y no se debe llamar al metodo save del repositorio.            
        }
    }
    
    
    
    public PreguntaDTO toDTO(Pregunta entidad) {
    	
    	PreguntaDTO dto = new PreguntaDTO();
    	
    	dto.setDescripcion(entidad.getDescripcion());
    	dto.setEstado(entidad.getEstado() != null ? Integer.valueOf(entidad.getEstado()) : null);
    	dto.setGrupo(entidad.getGrupo());
    	dto.setId(entidad.getId());
    	dto.setIdCuestionario(entidad.getIdCuestionario());
    	dto.setObligatorio(entidad.getObligatorio());
    	dto.setOrden(entidad.getOrden());
    	dto.setTipo(entidad.getTipo());
    	dto.setIdSiguientePregunta(entidad.getIdSiguientePregunta());
    	
    	// Cargar opciones SOLO si es opción múltiple
        if ("O".equals(entidad.getTipo())) {
            List<OpcionDTO> opcionesDto = opcionService.listarPorPregunta(entidad.getId());
            dto.setOpciones(opcionesDto);
        }
    	
    	return dto;
    }        

}
