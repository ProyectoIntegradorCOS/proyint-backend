package pe.gob.onp.thaqhiri.util;

public class UConversion {
	
	public static Integer toIntegerOrNull(String value) {
	    if (value == null) {
	        return null;
	    }
	    try {
	        return Integer.valueOf(value);
	    } catch (NumberFormatException e) {
	        return null;
	    }
	}
	
	public static Long toLongOrNull(String value) {
	    if (value == null) {
	        return null;
	    }
	    try {
	        return Long.valueOf(value);
	    } catch (NumberFormatException e) {
	        return null;
	    }
	}

}
