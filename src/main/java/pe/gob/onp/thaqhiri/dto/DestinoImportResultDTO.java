package pe.gob.onp.thaqhiri.dto;

import java.util.List;

public class DestinoImportResultDTO {

    private int filasProcesadas;
    private int destinosCreados;
    private int destinosReusados;
    private int geocodificados;
    private int sinTokenMapbox;
    private List<String> errores;

    public int getFilasProcesadas() {
        return filasProcesadas;
    }

    public void setFilasProcesadas(int filasProcesadas) {
        this.filasProcesadas = filasProcesadas;
    }

    public int getDestinosCreados() {
        return destinosCreados;
    }

    public void setDestinosCreados(int destinosCreados) {
        this.destinosCreados = destinosCreados;
    }

    public int getDestinosReusados() {
        return destinosReusados;
    }

    public void setDestinosReusados(int destinosReusados) {
        this.destinosReusados = destinosReusados;
    }

    public int getGeocodificados() {
        return geocodificados;
    }

    public void setGeocodificados(int geocodificados) {
        this.geocodificados = geocodificados;
    }

    public int getSinTokenMapbox() {
        return sinTokenMapbox;
    }

    public void setSinTokenMapbox(int sinTokenMapbox) {
        this.sinTokenMapbox = sinTokenMapbox;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores;
    }
}

