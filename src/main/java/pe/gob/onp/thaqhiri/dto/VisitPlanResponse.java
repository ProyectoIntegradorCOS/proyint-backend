package pe.gob.onp.thaqhiri.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import pe.gob.onp.thaqhiri.model.VisitPlanStatus;

public record VisitPlanResponse(
        Long id,
        String title,
        LocalDate plannedFor,
        VisitPlanStatus status,
        Long verifierId,        
        String verifierNombre,
        String equipoNombre,
        Long idEquipo,
        Long idUsuario,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Expone coordenadas y fecha de inicio del plan para pintar el punto I en seguimiento web][obj: VisitPlanResponse start coords]
        Double startLatitude,
        Double startLongitude,
        OffsetDateTime startAt,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Expone coordenadas y fecha de fin del plan][obj: VisitPlanResponse finish coords]
        Double endLatitude,
        Double endLongitude,
        OffsetDateTime endAt,
        List<VisitItemResponse> items
) {
}
