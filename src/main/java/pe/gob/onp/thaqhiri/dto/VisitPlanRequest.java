package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record VisitPlanRequest(
		Long id,
		@NotNull(message = "El colaborador es requerido")
        Long verifierId,
        @Size(max = 150, message = "El título no debe exceder 150 caracteres")
        String title,
        LocalDate plannedFor,
        @Valid
        List<VisitItemCreateRequest> items,
        String usuarioSesion
) {
}
