package pe.gob.onp.thaqhiri.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import pe.gob.onp.thaqhiri.model.VisitItemState;

public record VisitItemResponse(
        Long id,
        String companyName,        
        OffsetDateTime targetTime,        
        Integer orderIndex,
        String prioridad,
        String plantillaPv,
        VisitItemState state,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String otherInfo,
        String direccion,
        Long destinoId,
        String destinoNombre,
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-06 00:00 UTC-5 (Lima)][desc: Incluye coordenadas del destino en el response del plan para cálculo de ruta óptima][obj: VisitItemResponse latitude/longitude]
        Double latitude,
        Double longitude
) {
}
