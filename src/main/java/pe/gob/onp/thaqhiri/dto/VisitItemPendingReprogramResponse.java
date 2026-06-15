package pe.gob.onp.thaqhiri.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import pe.gob.onp.thaqhiri.model.VisitItemState;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Response para bandeja de pendientes a reprogramar][obj: VisitItemPendingReprogramResponse]
public record VisitItemPendingReprogramResponse(
        Long itemId,
        String companyName,
        String direccion,
        String prioridad,
        String plantillaPv,
        VisitItemState state,
        OffsetDateTime targetTime,
        Long planId,
        LocalDate plannedFor,
        Long verifierId,
        String verifierNombre,
        Long equipoId,
        String equipoNombre
) {
}
