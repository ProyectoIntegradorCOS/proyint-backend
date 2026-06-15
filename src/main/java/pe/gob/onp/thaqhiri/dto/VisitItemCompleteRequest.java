package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record VisitItemCompleteRequest(
        Boolean complex,
        Boolean foundProblem,
        @Size(max = 500, message = "El detalle de problema no debe exceder 500 caracteres")
        String problemNote,
        @Size(max = 500, message = "La información adicional no debe exceder 500 caracteres")
        String otherInfo,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Recibe coordenadas del verificador al completar la visita][obj: VisitItemCompleteRequest event coords]
        Double eventLatitude,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-14 11:27 UTC-5 (Lima)][desc: Recibe coordenadas del verificador al completar la visita][obj: VisitItemCompleteRequest event coords]
        Double eventLongitude,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-03-19 UTC-5 (Lima)][desc: Fecha/hora real del evento en el dispositivo del verificador (offline o en línea)][obj: VisitItemCompleteRequest occurredAt]
        OffsetDateTime occurredAt
) {
}
