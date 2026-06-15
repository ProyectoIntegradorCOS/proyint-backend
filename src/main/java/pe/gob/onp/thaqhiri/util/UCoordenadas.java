package pe.gob.onp.thaqhiri.util;

public class UCoordenadas {
	
	    // Límites geográficos de Perú
	    private static final double PERU_LAT_MIN = -18.35;
	    private static final double PERU_LAT_MAX = -0.03;
	    private static final double PERU_LON_MIN = -81.35;
	    private static final double PERU_LON_MAX = -68.65;

	    /**
	     * Retorna true si las coordenadas están fuera del territorio peruano
	     */
	    public static boolean estaFueraDePeru(Double latitud, Double longitud) {

	        if (latitud == null || longitud == null) {
	            return true; // coordenadas inválidas
	        }

	        return latitud < PERU_LAT_MIN
	            || latitud > PERU_LAT_MAX
	            || longitud < PERU_LON_MIN
	            || longitud > PERU_LON_MAX;
	    }
	}
	