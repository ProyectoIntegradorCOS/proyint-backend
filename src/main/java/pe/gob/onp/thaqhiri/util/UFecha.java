package pe.gob.onp.thaqhiri.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UFecha {

	private static final DateTimeFormatter FORMATO_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
    public static String generarTimestampCompleto() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        return LocalDateTime.now().format(formatter);
    }
    
    public static String localDateToString(LocalDate fecha) {
        if (fecha == null) {
            return null;
        }
        return fecha.format(FORMATO_DD_MM_YYYY);
    }
}
