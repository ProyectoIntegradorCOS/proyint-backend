package pe.gob.onp.thaqhiri.model;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:07 UTC-5 (Lima)][desc: Amplía eventos para registrar cambios de PV (prioridad/plantilla/destino/dirección/hora/nombre) en histórico][obj: VisitItemEventType]
public enum VisitItemEventType {
    ORDER_CHANGE,
    STATE_CHANGE,
    PRIORITY_CHANGE,
    PV_TEMPLATE_CHANGE,
    DESTINO_CHANGE,
    ADDRESS_CHANGE,
    TARGET_TIME_CHANGE,
    COMPANY_NAME_CHANGE,
    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-21 14:43 UTC-5 (Lima)][desc: Registra marcado y reasignacion de pendientes][obj: VisitItemEventType reprogramacion]
    REPROGRAM_MARK,
    REPROGRAM_ASSIGN
}
