package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pe.gob.onp.thaqhiri.entity.Destino;

import java.time.OffsetDateTime;

public record VisitItemCreateRequest(
		Long id,   // id opcional, puede ser null para items nuevos
        @NotBlank(message = "La empresa es requerida")
        @Size(max = 200, message = "La empresa no debe exceder 200 caracteres")
        String companyName,
        OffsetDateTime targetTime,
        String direccion,
        String prioridad,
        String plantillaPv,
        Long destinoId
) {
}
