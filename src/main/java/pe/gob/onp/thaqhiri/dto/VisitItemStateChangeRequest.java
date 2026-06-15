package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.constraints.NotNull;
import pe.gob.onp.thaqhiri.model.VisitItemState;

import java.time.OffsetDateTime;

public record VisitItemStateChangeRequest(
        @NotNull(message = "El nuevo estado es requerido")
        VisitItemState newState,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Recibe coordenadas del verificador al iniciar el plan][obj: VisitItemStateChangeRequest start coords]
        Double startLatitude,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-07 15:32 UTC-5 (Lima)][desc: Recibe coordenadas del verificador al iniciar el plan][obj: VisitItemStateChangeRequest start coords]
        Double startLongitude,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Recibe coordenadas del verificador al cambiar el estado de la visita][obj: VisitItemStateChangeRequest event coords]
        Double eventLatitude,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Recibe coordenadas del verificador al cambiar el estado de la visita][obj: VisitItemStateChangeRequest event coords]
        Double eventLongitude,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Fecha/hora real del evento en el dispositivo del verificador (offline o en línea)][obj: VisitItemStateChangeRequest occurredAt]
        OffsetDateTime occurredAt
) {
}
