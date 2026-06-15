package pe.gob.onp.thaqhiri.dto;

import java.util.List;

/**
 * DTO genérico para estandarizar las respuestas de la API.
 * Contiene metadatos (código, mensaje) y el array de resultados.
 */
public class RespuestaDTO<T> {
    private String codigoResultado;
    private String mensajeResultado;
    private List<T> resultados;

    // 1. Constructor para respuestas exitosas o con datos
    public RespuestaDTO(String codigoResultado, String mensajeResultado, List<T> resultados) {
        this.codigoResultado = codigoResultado;
        this.mensajeResultado = mensajeResultado;
        this.resultados = resultados;
    }

    // 2. Constructor por defecto (necesario para Jackson/JSON)
    public RespuestaDTO() {}
    
    // --- Getters y Setters ---

    public String getCodigoResultado() { 
        return codigoResultado; 
    }
    public void setCodigoResultado(String codigoResultado) { 
        this.codigoResultado = codigoResultado; 
    }
    public String getMensajeResultado() { 
        return mensajeResultado; 
    }
    public void setMensajeResultado(String mensajeResultado) { 
        this.mensajeResultado = mensajeResultado; 
    }
    public List<T> getResultados() { 
        return resultados; 
    }
    public void setResultados(List<T> resultados) { 
        this.resultados = resultados; 
    }
}
