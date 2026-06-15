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
import pe.gob.onp.thaqhiri.util.UConstante;

import java.util.List;
import java.util.Optional;

@Service
public class OpcionService {

    @Autowired
    private OpcionRepository repository;

    // Buscar con paginación
    public List<OpcionDTO> listarPorPregunta(Long idPregunta) {
        
    	List<Opcion> opciones = repository.findByIdPreguntaAndEstadoOrderByOrden(idPregunta, UConstante.ACTIVO_REGI);        
    	
    	// Convertimos a DTO usando Stream y el método toDTO
        List<OpcionDTO> resultado = opciones.stream()
                                               .map(this::toDTO)
                                               .toList();
        
        return resultado;
    }

    // Crear opción
    public OpcionDTO crear(OpcionDTO dto, String usuario, String terminal){
        Opcion o = new Opcion();
        
        o.setIdPregunta(dto.getIdPregunta());
        o.setDescripcion(dto.getDescripcion());
        o.setValor(dto.getValor());
        o.setEstado(dto.getEstado() != null ? String.valueOf(dto.getEstado()) : UConstante.ACTIVO_REGI);
        o.setOrden(dto.getOrden());
        o.setIdSiguientePregunta(dto.getIdSiguientePregunta());
        o.setUsuarioCrea(usuario);
        o.setTerminalCrea(terminal);
        
        return toDTO(repository.save(o));
    }

    // Actualizar opción
    public OpcionDTO actualizar(Long id, OpcionDTO dto, String usuario, String terminal){
        Optional<Opcion> optional = repository.findById(id);
        if(optional.isPresent()){
            Opcion o = optional.get();
            o.setDescripcion(dto.getDescripcion());
            o.setValor(dto.getValor());
            o.setEstado(dto.getEstado() != null ? String.valueOf(dto.getEstado()) : UConstante.ACTIVO_REGI);
            o.setOrden(dto.getOrden());
            o.setIdSiguientePregunta(dto.getIdSiguientePregunta());
            o.setUsuarioModi(usuario);
            o.setTerminalModi(terminal);
            return toDTO(repository.save(o));
        }
        throw new RuntimeException("Opción no encontrada");
    }

    // Eliminar opción
    public void eliminar(Long id, String usuario, String terminal){
        repository.desactivar(id, usuario, terminal);
    }
    
	public OpcionDTO toDTO(Opcion entidad) {
	    	
			OpcionDTO dto = new OpcionDTO();
	    	
	    	dto.setDescripcion(entidad.getDescripcion());
	    	dto.setEstado(entidad.getEstado() != null ? Integer.valueOf(entidad.getEstado()) : null);
	    	dto.setId(entidad.getId());
	    	dto.setIdPregunta(entidad.getIdPregunta());
	    	dto.setValor(entidad.getValor());
	    	dto.setIdSiguientePregunta(entidad.getIdSiguientePregunta());
	    	dto.setOrden(entidad.getOrden());
	    	
	    	return dto;
	}
	
	

    @Transactional
    public void sincronizarOpciones(Long idPregunta,
                                    List<OpcionDTO> opcionesFrontend,
                                    String usuario,
                                    String terminal) {

        // Obtener las opciones actuales en BD
        List<Opcion> opcionesBD = repository
                .findByIdPreguntaAndEstadoOrderByOrden(idPregunta, UConstante.ACTIVO_REGI);

        // Marcar todas como eliminadas (borrado lógico)
        for (Opcion o : opcionesBD) {
            o.setEstado(UConstante.INACTIVO_REGI);
            o.setUsuarioModi(usuario);
            o.setTerminalModi(terminal);
        }

        int nuOrden = 1;
        
        // Procesar las que vienen del frontend
        for (OpcionDTO dto : opcionesFrontend) {

            if (dto.getId() != null) {
                // La opción ya existía → actualizarla
                Opcion o = opcionesBD.stream()
                        .filter(x -> x.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Opción no encontrada: " + dto.getId()));

                o.setDescripcion(dto.getDescripcion());
                o.setIdSiguientePregunta(dto.getIdSiguientePregunta());
                o.setOrden(nuOrden++);
                o.setEstado(UConstante.ACTIVO_REGI);
                o.setUsuarioModi(usuario);
                o.setTerminalModi(terminal);

            } else {
                // Es una opción nueva → crear
                Opcion nueva = new Opcion();
                nueva.setIdPregunta(idPregunta);
                nueva.setDescripcion(dto.getDescripcion());
                nueva.setIdSiguientePregunta(dto.getIdSiguientePregunta());
                nueva.setOrden(nuOrden++);
                nueva.setEstado(UConstante.ACTIVO_REGI);
                nueva.setUsuarioCrea(usuario);
                nueva.setTerminalCrea(terminal);

                repository.save(nueva);
            }
        }

        // Guardar cambios (Hibernate sincroniza todo al cerrar la transacción)
    }
    
    
    
    @Transactional
    public void desactivarPorPregunta(Long idPregunta, String usuario, String terminal) {

    	repository.desactivarPorPregunta(
            idPregunta,
            usuario,
            terminal
        );
    }

    
    

}
