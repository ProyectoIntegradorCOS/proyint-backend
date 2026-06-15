package pe.gob.onp.thaqhiri.model;

public enum VisitItemState {
    PENDING,
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Soporta estado pendiente de reprogramacion][obj: VisitItemState PENDING_REPROGRAMAR]
    PENDING_REPROGRAMAR,
    EN_ROUTE,
    ON_SITE,
    IN_VISIT,
    DONE,
    CANCELLED,
    DELETED
}
