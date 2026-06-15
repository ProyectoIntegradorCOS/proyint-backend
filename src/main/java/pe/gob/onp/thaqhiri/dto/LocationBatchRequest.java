package pe.gob.onp.thaqhiri.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-05 08:20 UTC-5 (Lima)][desc: DTO para solicitud de creación de ubicaciones en lote][obj: LocationBatchRequest]
@Schema(description = "Solicitud de creación de múltiples ubicaciones (batch)")
public record LocationBatchRequest(
        @Schema(description = "Lista de ubicaciones a registrar", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La lista de ubicaciones no puede ser nula")
        @NotEmpty(message = "La lista de ubicaciones no puede estar vacía")
        @Valid
        List<LocationCreateRequest> locations
) {}
