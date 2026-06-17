package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.dto.HorarioDTO;
import pe.gob.onp.thaqhiri.entity.Horario;
import pe.gob.onp.thaqhiri.repository.HorarioRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HorarioService {

    private final HorarioRepository repository;

    public HorarioService(HorarioRepository repository) {
        this.repository = repository;
    }

    public List<HorarioDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<HorarioDTO> getById(Long id) {
        return repository.findById(id).map(this::toDTO);
    }

    public HorarioDTO create(HorarioDTO dto, String usuario, String terminal) {
        Horario entity = toEntity(dto);        
        
        entity.setIdUsuaCrea(usuario);
        entity.setDeTermCrea(terminal);
        
        Horario saved = repository.save(entity);
        return toDTO(saved);
    }

    public HorarioDTO update(Long id, HorarioDTO dto, String usuario, String terminal) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNombre(dto.getNombre());
                    existing.setHoraInicio(dto.getHoraInicio());
                    existing.setHoraFin(dto.getHoraFin());
                    existing.setEstado(dto.getEstado() != null ? dto.getEstado() : UConstante.ACTIVO_REGI);
                    existing.setIdUsuaModi(usuario);
                    existing.setDeTermModi(terminal);
                    
                    Horario updated = repository.save(existing);
                    
                    return toDTO(updated);
                }).orElseThrow(() -> new RuntimeException("Horario no encontrado"));
    }


    private HorarioDTO toDTO(Horario entity) {
        HorarioDTO dto = new HorarioDTO();
        
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setHoraInicio(entity.getHoraInicio());
        dto.setHoraFin(entity.getHoraFin());
        dto.setEstado(entity.getEstado());
        
        return dto;
    }

    private Horario toEntity(HorarioDTO dto) {
        Horario entity = new Horario();
        
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setHoraInicio(dto.getHoraInicio());
        entity.setHoraFin(dto.getHoraFin());
        entity.setEstado(dto.getEstado() != null ? dto.getEstado() : UConstante.ACTIVO_REGI);
        
        return entity;
    }
}
