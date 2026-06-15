package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Request para reprogramar visita pendiente][obj: VisitItemReassignRequest]
public record VisitItemReassignRequest(
        @NotNull Long newVerifierId,
        @NotNull LocalDate newPlannedFor,
        String reason
) {
}
