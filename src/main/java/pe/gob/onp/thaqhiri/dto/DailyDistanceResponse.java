package pe.gob.onp.thaqhiri.dto;

import java.time.LocalDate;

public record DailyDistanceResponse(
        String saaSubject,
        LocalDate date,
        double distanceKm
) {
}
