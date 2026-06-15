package pe.gob.onp.thaqhiri.service;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.gob.onp.thaqhiri.entity.Destino;
import pe.gob.onp.thaqhiri.exception.BusinessException;
import pe.gob.onp.thaqhiri.model.FilaDestinoImport;
import pe.gob.onp.thaqhiri.model.ResultadoValidacion;
import pe.gob.onp.thaqhiri.model.TipoResultadoValidacion;
import pe.gob.onp.thaqhiri.repository.DestinoRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Duration;

@Service
@Transactional
public class DestinoImportService {
	
	private static final Logger log = LoggerFactory.getLogger(DestinoImportService.class);

    private final DestinoRepository destinoRepository;
    private final DestinoService destinoService;
    private final MapboxGeocodingService mapboxGeocodingService;
    private final ImportJobService importJobService;
    private final DestinoImportBatchService destinoImportBatchService;
    

    public DestinoImportService(
            DestinoRepository destinoRepository,
            DestinoService destinoService,
            MapboxGeocodingService mapboxGeocodingService,
            ImportJobService importJobService,
            DestinoImportBatchService destinoImportBatchService
    ) {
        this.destinoRepository = destinoRepository;
        this.destinoService = destinoService;
        this.mapboxGeocodingService = mapboxGeocodingService;
        this.importJobService = importJobService;
        this.destinoImportBatchService = destinoImportBatchService;
    }

    




    private void fillDestinoRow(Row r, Destino d) {
        r.createCell(0).setCellValue(d.getNombre() != null ? d.getNombre() : "");
        r.createCell(1).setCellValue(d.getDireccion() != null ? d.getDireccion() : "");
        r.createCell(2).setCellValue("");
        r.createCell(3).setCellValue("");
        r.createCell(4).setCellValue("");
        r.createCell(5).setCellValue("NO");
        r.createCell(6).setCellValue(d.getCategoria() != null ? d.getCategoria() : "");
        var lat = r.createCell(7);
        if (d.getLatitud() != null) lat.setCellValue(d.getLatitud());
        else lat.setBlank();

        var lon = r.createCell(8);
        if (d.getLongitud() != null) lon.setCellValue(d.getLongitud());
        else lon.setBlank();
        r.createCell(9).setCellValue(d.getPrecision() != null ? d.getPrecision() : "");
        r.createCell(10).setCellValue(UConstante.ACTIVO_REGI.equals(d.getEstadoRegistro()) ? "ACTIVO" : "INACTIVO");
    }

    private Map<String, Integer> parseHeader(Sheet sheet) {
        Map<String, Integer> map = new HashMap<>();
        Row header = sheet.getRow(2);
        
        if (header == null) {
            throw new BusinessException("El Excel no tiene fila de cabecera");
        }
        
        for (Cell cell : header) {
            String v = cellToString(cell);
            if (v == null) continue;
            String key = normalizeHeader(v);
            map.put(key, cell.getColumnIndex());
        }
        
        return map;
    }

    
    private String normalizeHeader(String raw) {

        String v = raw.trim().toLowerCase(Locale.ROOT);

        v = v.replace("á","a")
             .replace("é","e")
             .replace("í","i")
             .replace("ó","o")
             .replace("ú","u")
             .replace("ñ","n");

        v = v.replaceAll("[^a-z0-9]+", "");

        return switch (v) {

            case "nombredeldestino" -> "nombredeldestino";

            case "direccion" -> "direccion";
            
            case "distrito" -> "distrito";
            
            case "provincia" -> "provincia";
            
            case "departamento" -> "departamento";

            case "ubicabilidad" -> "ubicabilidad";

            case "estadoonp" -> "estadoonp";

            case "fechaactualizacion" -> "fechaactualizacion";

            default -> v;
        };
    }
    

    private String readCell(Row row, Map<String, Integer> headerMap, String key) {
        
    	String respuesta = null;
    	
    	try {
	    	Integer idx = headerMap.get(key);
	    	
	        if (idx == null) return null;
	        
	        Cell cell = row.getCell(idx);
	        
	        if (cell == null) return null;
	        
	        respuesta = cellToString(cell);
    	}
    	catch(Exception ex) {
    		respuesta = null;
    	}
        
        return respuesta;
    }

    private String cellToString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception ex) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    //private String buildFullAddress(String direccion, String distrito, String provincia, String departamento) {
    private String buildFullAddress(String direccion) {
        List<String> parts = new ArrayList<>();
        
        if (!isBlank(direccion)) parts.add(direccion.trim());
        /*
        if (!isBlank(distrito)) parts.add(distrito.trim());
        if (!isBlank(provincia)) parts.add(provincia.trim());
        if (!isBlank(departamento)) parts.add(departamento.trim());
        */
        
        if (parts.isEmpty()) return null;
        return String.join(", ", parts);
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private String trimOrNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private Double round6(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }
    
    
    public ResultadoValidacion validarExcel(
            MultipartFile file,
            String usuarioSesion,
            String terminalSesion
    ) {
    	ResultadoValidacion resultado = new ResultadoValidacion();
    	resultado.setTipoResultado(TipoResultadoValidacion.CON_ERRORES); //Valor predeterminado
    	
        if (file == null || file.isEmpty()) {
        	resultado.setMensaje("Debe adjuntar un archivo Excel (.xlsx)");
        	return resultado;
        }
        
        if (usuarioSesion == null || usuarioSesion.isBlank()) {
            resultado.setMensaje("Usuario de sesión requerido");
        	return resultado;
        }
        
        if (terminalSesion == null || terminalSesion.isBlank()) {
            resultado.setMensaje("Terminal de sesión requerida");
        	return resultado;
        }

        //Procesamiento del archivo
        try (InputStream in = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                resultado.setMensaje("El Excel no contiene hojas");
            	return resultado;
            }

            Map<String, Integer> headerMap = parseHeader(sheet);
            
            if (headerMap == null || !headerMap.containsKey("nombredeldestino") || !headerMap.containsKey("direccion")) {
                resultado.setMensaje("El archivo no tiene el formato requerido");
            	return resultado;
            }

            int rowNum = 0;
            int numFilasDatos = 0;
            
            for (Row row : sheet) {
                rowNum++;
                if (row.getRowNum() < 3) {
                	continue; // header
                }                
                
                String nombre = readCell(row, headerMap, "nombredeldestino");
                String direccion = readCell(row, headerMap, "direccion");

                if (isBlank(nombre) && isBlank(direccion)) {
                    continue;
                }
                
                if (isBlank(nombre)) {
                    resultado.setMensaje("Fila " + rowNum + ": nombre vacío");
                	return resultado;                    
                }
                
                nombre = nombre.trim();
                
                if (isBlank(direccion)) {
                    resultado.setMensaje("Fila " + rowNum + ": dirección vacía");
                	return resultado;                    
                }

                direccion = direccion.trim();
                
                numFilasDatos++;
            }

            if( numFilasDatos == 0) {
            	resultado.setMensaje("El Excel no contiene filas de datos para importar.");
            	return resultado;
            }
            
            //Si llega a este punto to Ok
            resultado.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);            		
            
            return resultado;
            
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Error importando Excel: " + ex.getMessage());
        }
    }

    
    
    
    private String buildKey(String nombre,
            String direccion,
            String ubicabilidad,
            String estado,
            LocalDate fecha) {

		 return normalizeText(nombre) + "|" +
				normalizeText(direccion) + "|" +
				normalizeText(ubicabilidad) + "|" +
				normalizeText(estado) + "|" +
				(fecha == null ? "" : fecha.toString());
	}

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase();
    }

    
    
    private String formatearDuracion(Instant inicio, Instant fin) {

        long totalSegundos = Duration.between(inicio, fin).getSeconds();

        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;

        StringBuilder tiempo = new StringBuilder();

        if (horas > 0) {
            tiempo.append(horas).append(" hora");
            if (horas > 1) tiempo.append("s");
        }

        if (minutos > 0) {
            if (tiempo.length() > 0) tiempo.append(", ");
            tiempo.append(minutos).append(" minuto");
            if (minutos > 1) tiempo.append("s");
        }

        if (segundos > 0 || tiempo.length() == 0) {
            if (tiempo.length() > 0) tiempo.append(", ");
            tiempo.append(segundos).append(" segundo");
            if (segundos != 1) tiempo.append("s");
        }

        return tiempo.toString();
    }
    
    
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void importExcelAsync(
    		Instant inicio,
    		byte[] fileBytes,
    		String originalFilename,
            String usuarioSesion,
            String terminalSesion,
            Long jobId
    ) {
    	log.info("importExcelAsync(), Inicio, jobId=" + jobId + ", originalFilename=" + originalFilename + ", fileBytes=" + fileBytes);    	

    	final int PROGRESS_SIZE = 30;
        final int BATCH_SIZE = 1000;

        try (InputStream in = new java.io.ByteArrayInputStream(fileBytes);
        	     XSSFWorkbook workbook = new XSSFWorkbook(in)) {

        	log.info("importExcelAsync(), jobId=" + jobId + ", archivo abierto");
        	
            Sheet sheet = workbook.getSheetAt(0);
            
            //log.info("importExcelAsync(), jobId=" + jobId + ", hoja 0 recuperada");

            Map<String, Integer> headerMap = parseHeader(sheet);
            List<FilaDestinoImport> filasFiltradas = limitarFilas(sheet, headerMap);
            importJobService.actualizarTotal(jobId, filasFiltradas.size());
            
            //log.info("importExcelAsync(), jobId=" + jobId + ", headerMap=" + headerMap);

            int filasNuevas = 0;
            int filasExistentes = 0;
            int procesadas = 0;

            //Obtiene los destinos activos existentes y los pone en memoria para evitar consultar constantemente a la base de datos
            List<Destino> existentes = destinoRepository.findByEstadoRegistro(UConstante.ACTIVO_REGI);
            
            log.info("importExcelAsync(), jobId=" + jobId + ", existentes cargados, total=" + existentes.size());
            
            Map<String, Destino> existentesMap = new HashMap<>();

            for (Destino d : existentes) {
                existentesMap.put(buildKey(d.getNombre(), d.getDireccion(), d.getUbicabilidadOnp(), d.getEstadoOnp(), d.getFechaActualizacionOnp()), d);
            }

            //Recorre las filas y por cada una evalua si es existente o nueva
            List<Destino> nuevos = new ArrayList<>();
            
            //log.info("importExcelAsync(), jobId=" + jobId + ", antes del for, sheet.getLastRowNum()=" + sheet.getLastRowNum());

            //for (int i = 3; i <= sheet.getLastRowNum(); i++) {
            for (FilaDestinoImport fila : filasFiltradas) {
            	
            	String nombre = fila.getNombre();
                String direccion = fila.getDireccion();                
                String ubicabilidad = fila.getUbicabilidad();
                String estadoOnp = fila.getEstadoOnp();
                LocalDate fechaActualizacion = fila.getFechaActualizacion();

                if (isBlank(nombre) || isBlank(direccion) || isBlank(ubicabilidad) || isBlank(estadoOnp) || fechaActualizacion==null) continue;
                
                //Normalizar los valores
                nombre = normalizeText(nombre);   
                direccion = normalizeText(direccion);
                ubicabilidad = normalizeText(ubicabilidad);
                estadoOnp = normalizeText(estadoOnp);

                String key = buildKey(nombre, direccion, ubicabilidad, estadoOnp, fechaActualizacion);

                if (!existentesMap.containsKey(key)) {
                    //Se trata de un nuevo destino
                	Destino entity = new Destino();
                    
                	entity.setNombre(nombre);
                    entity.setDireccion(direccion);     
                    entity.setPrecision("APROXIMADO");
                    entity.setUbicabilidadOnp(ubicabilidad);
                    entity.setEstadoOnp(estadoOnp);
                    entity.setFechaActualizacionOnp(fechaActualizacion);
                    entity.setEstadoRegistro(UConstante.ACTIVO_REGI);
                    entity.setUsuarioCreacion(usuarioSesion);
                    entity.setTerminalCreacion(terminalSesion);                    
                    
                    //Como el destino es nuevo, se intenta obtener las coordenadas para registrar
                    var sug = mapboxGeocodingService.forwardGeocode(direccion);
                    
                    if (sug.isEmpty()) {
                    	//Se cancela toda la transaccion, debido a que no se encuentran las coordenadas 
                    	throw new BusinessException("No se encontraron las coordenadas para el destino '" + nombre + "' con dirección '" + direccion + "'.");
                    }
                    else {
                    	var s = sug.get();                    	
	                    entity.setLatitud(s.lat());
	                    entity.setLongitud(s.lng());
                    }                    
                    
                    nuevos.add(entity);
                    filasNuevas = filasNuevas + 1;
                    existentesMap.put(key, entity);
                }
                else {
                	filasExistentes = filasExistentes + 1;                    
                }

                procesadas++;

                //Cada cierta cantidad de filas procesadas se actualiza el avance
                if (procesadas % PROGRESS_SIZE == 0) {
                    importJobService.actualizarProgreso(jobId, procesadas);
                }

                //Cada cierta cantidad de filas se guarda por lotes en la BD
                if (nuevos.size() == BATCH_SIZE) {
                	destinoImportBatchService.guardarLote(nuevos);
                    nuevos.clear();
                }
            }

            //Si quedó algun lote sin guardar en la BD entonces aca se realiza
            if (!nuevos.isEmpty()) {
            	destinoImportBatchService.guardarLote(nuevos);
            }            

             // ⏱️ Momento final
            Instant fin = Instant.now();            
            String tiempoFormateado = formatearDuracion(inicio, fin);

            String mensajeFinal = "Importación finalizada. " +
				                  "Nuevos=" + filasNuevas +
//				                  ", Existentes=" + filasExistentes +
				                  ", Tiempo=" + tiempoFormateado + ".";
            
            importJobService.completar(jobId, mensajeFinal);

        } catch (Exception e) {
            importJobService.error(jobId, e.getMessage());
        }
    }
    
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarLote(List<Destino> lote) {
        destinoRepository.saveAll(lote);
        destinoRepository.flush();
    }
    
    
    
    private List<FilaDestinoImport> limitarFilas(
            Sheet sheet,
            Map<String, Integer> headerMap
    ) {

    	//Este primer bloque obtiene todas las filas del Excel, sin filtrar ninguna.
        List<FilaDestinoImport> filas = new ArrayList<>();

        for (int i = 3; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            String nombre = readCell(row, headerMap, "nombredeldestino");
            String direccion = readCell(row, headerMap, "direccion");
            String distrito = readCell(row, headerMap, "distrito");
            String provincia = readCell(row, headerMap, "provincia");
            String departamento = readCell(row, headerMap, "departamento");            
            String ubicabilidad = readCell(row, headerMap, "ubicabilidad");
            String estadoOnp = readCell(row, headerMap, "estadoonp");
            LocalDate fechaActualizacion = readDateCell(row, headerMap, "fechaactualizacion");

            if (isBlank(nombre) || isBlank(direccion) || isBlank(distrito) || isBlank(provincia) || isBlank(departamento) || 
            	isBlank(ubicabilidad)  || isBlank(estadoOnp)  || fechaActualizacion == null)
            { 
            	continue;
            }

            //Concatenar la direccion
            direccion = direccion + ", " + distrito + ", " + provincia + ", " + departamento;
            
            FilaDestinoImport fila = new FilaDestinoImport();
            fila.setNombre(normalizeText(nombre));
            fila.setDireccion(direccion.trim());
            fila.setUbicabilidad(ubicabilidad);
            fila.setEstadoOnp(estadoOnp);
            fila.setFechaActualizacion(fechaActualizacion);

            filas.add(fila);
        }

        //Este segundo bloque agrupa los destinos por nombre y direccion, cada clave de grupo tiene su lista de filas.
        Map<String, List<FilaDestinoImport>> grupos = new HashMap<>();

        for (FilaDestinoImport fila : filas) {

            String key =
                    normalizeText(fila.getNombre()) + "|" +
                    normalizeText(fila.getDireccion());

            grupos.computeIfAbsent(key, k -> new ArrayList<>()).add(fila);
        }

        //Este tercer bloque ordena por fecha de actualizacion descendente y obtiene hasta las 5 filas mas recientes como maximo por cada grupo.
        List<FilaDestinoImport> resultado = new ArrayList<>();

        for (List<FilaDestinoImport> grupo : grupos.values()) {

        	//Primero ordena las filas de cada grupo por fecha de actualizacion descendente, solo si hay mas de 5 filas
        	log.info("grupo.size()=" + grupo.size());
        	
        	if(grupo.size() > 5) {	        	
	            grupo.sort(
	                    Comparator.comparing(
	                            FilaDestinoImport::getFechaActualizacion,
	                            Comparator.nullsLast(LocalDate::compareTo)
	                    ).reversed()
	            );
        	}

            //Luego determina el tamaño de filas a obtener (limita a 5 las filas o si el total es menor mantiene el tamaño del grupo).
            int limite = Math.min(5, grupo.size());

            //Finalmente agrega las filas del grupo a la lista de filas del resultado final 
            for (int i = 0; i < limite; i++) {
                resultado.add(grupo.get(i));
            }
        }

        return resultado;
    }
    
    
    private LocalDate readDateCell(Row row, Map<String,Integer> headerMap, String key) {

        Integer idx = headerMap.get(key);
        if (idx == null) return null;

        Cell cell = row.getCell(idx);
        if (cell == null) return null;

        try {

            if (cell.getCellType() == CellType.NUMERIC &&
                DateUtil.isCellDateFormatted(cell)) {

                return cell.getLocalDateTimeCellValue().toLocalDate();
            }

            if (cell.getCellType() == CellType.STRING) {

                String v = cell.getStringCellValue();
                if (v == null || v.isBlank()) return null;

                return LocalDate.parse(v);
            }

        } catch (Exception ex) {
        	log.error("Error al leer el valor Date de una celda, key=" + key + ", RowNum=" + row.getRowNum());
            return null;
        }

        return null;
    }
    
}
