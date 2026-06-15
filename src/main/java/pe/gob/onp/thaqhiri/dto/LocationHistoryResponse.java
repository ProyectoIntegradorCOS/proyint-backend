package pe.gob.onp.thaqhiri.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record LocationHistoryResponse(
        String saaSubject,
        OffsetDateTime start,
        OffsetDateTime end,
        List<LocationResponse> points,
        double totalDistanceKm
) {
}
