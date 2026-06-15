package pe.gob.onp.thaqhiri.dto;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:58 UTC-5 (Lima)][desc: DTO para carga masiva interna (JSON) del catálogo de destinos][obj: DestinoImportRowDTO]
public record DestinoImportRowDTO(
        String nombreCompleto,
        String direccion,
        String dep,
        String prov,
        String dist
) {}

