package pe.gob.onp.thaqhiri.dto;

import java.util.List;

public class RespuestaBusquedaDTO<T> {
    private int codigoResultado;
    private String mensajeResultado;
    private List<T> resultados;
    private int paginaActual;
    private int totalPaginas;
    private int tamanioPagina;
    private long totalRegistros;

    public RespuestaBusquedaDTO() {}

    public RespuestaBusquedaDTO(int codigoResultado, String mensajeResultado, List<T> resultados, int totalPaginas) {
        this.codigoResultado = codigoResultado;
        this.mensajeResultado = mensajeResultado;
        this.resultados = resultados;
        this.totalPaginas = totalPaginas;
    }

    public int getCodigoResultado() {
        return codigoResultado;
    }

    public void setCodigoResultado(int codigoResultado) {
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

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

	public int getPaginaActual() {
		return paginaActual;
	}

	public void setPaginaActual(int paginaActual) {
		this.paginaActual = paginaActual;
	}

	public int getTamanioPagina() {
		return tamanioPagina;
	}

	public void setTamanioPagina(int tamanioPagina) {
		this.tamanioPagina = tamanioPagina;
	}

	public long getTotalRegistros() {
		return totalRegistros;
	}

	public void setTotalRegistros(long totalRegistros) {
		this.totalRegistros = totalRegistros;
	}
    
	
    
}
