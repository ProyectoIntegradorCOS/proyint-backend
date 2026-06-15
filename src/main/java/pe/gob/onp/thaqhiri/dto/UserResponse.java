package pe.gob.onp.thaqhiri.dto;

public record UserResponse(
        Long id,
        String saaSubject,
        String usuario,
        String nombre,
        Integer estado,
        String estadoDescripcion, 
        Integer equipoId,        
        String equipoNombre,
        Long horarioId,
        String horarioNombre
) {
}
