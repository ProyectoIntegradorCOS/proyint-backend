package pe.gob.onp.thaqhiri.dto;

import java.util.ArrayList;
import java.util.List;

// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:07 UTC-5 (Lima)][desc: DTO de resultado para import masivo de planes de visita (Excel) con resumen y errores por fila][obj: VisitPlanImportResultDTO]
public class VisitPlanImportResultDTO {

    private int planesCreados;
    private int planesActualizados;
    private int itemsLeidos;
    private int itemsProcesados;
    private List<ImportErrorDTO> errores = new ArrayList<>();

    public int getPlanesCreados() {
        return planesCreados;
    }

    public void setPlanesCreados(int planesCreados) {
        this.planesCreados = planesCreados;
    }

    public int getPlanesActualizados() {
        return planesActualizados;
    }

    public void setPlanesActualizados(int planesActualizados) {
        this.planesActualizados = planesActualizados;
    }

    public int getItemsLeidos() {
        return itemsLeidos;
    }

    public void setItemsLeidos(int itemsLeidos) {
        this.itemsLeidos = itemsLeidos;
    }

    public int getItemsProcesados() {
        return itemsProcesados;
    }

    public void setItemsProcesados(int itemsProcesados) {
        this.itemsProcesados = itemsProcesados;
    }

    public List<ImportErrorDTO> getErrores() {
        return errores;
    }

    public void setErrores(List<ImportErrorDTO> errores) {
        this.errores = errores != null ? errores : new ArrayList<>();
    }

    public void addError(int filaExcel, String campo, String mensaje) {
        this.errores.add(new ImportErrorDTO(filaExcel, campo, mensaje));
    }

    public static class ImportErrorDTO {
        private int filaExcel;
        private String campo;
        private String mensaje;

        public ImportErrorDTO() {}

        public ImportErrorDTO(int filaExcel, String campo, String mensaje) {
            this.filaExcel = filaExcel;
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public int getFilaExcel() {
            return filaExcel;
        }

        public void setFilaExcel(int filaExcel) {
            this.filaExcel = filaExcel;
        }

        public String getCampo() {
            return campo;
        }

        public void setCampo(String campo) {
            this.campo = campo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}
