package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
		Long id,
        @NotBlank String saaSubject,
        @NotBlank String usuario,
        @NotBlank String nombre,
        @NotNull Integer estado,
        Integer equipoId,
        @NotNull Long horarioId,
        @Email String email,
        String usuarioSesion
) {
}
