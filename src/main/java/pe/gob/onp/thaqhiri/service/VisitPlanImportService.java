package pe.gob.onp.thaqhiri.service;

import org.apache.poi.ss.usermodel.*;
import pe.gob.onp.thaqhiri.service.MapboxGeocodingService;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import pe.gob.onp.thaqhiri.dto.DestinoDTO;
import pe.gob.onp.thaqhiri.dto.VisitItemCreateRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanImportResultDTO;
import pe.gob.onp.thaqhiri.dto.VisitPlanRequest;
import pe.gob.onp.thaqhiri.dto.VisitPlanValidaResultDTO;
import pe.gob.onp.thaqhiri.entity.Destino;
import pe.gob.onp.thaqhiri.entity.User;
import pe.gob.onp.thaqhiri.exception.BusinessException;
import pe.gob.onp.thaqhiri.model.ResultadoValidacion;
import pe.gob.onp.thaqhiri.model.TipoPrioridad;
import pe.gob.onp.thaqhiri.model.TipoResultadoValidacion;
import pe.gob.onp.thaqhiri.model.VisitItemMasivo;
import pe.gob.onp.thaqhiri.repository.DestinoRepository;
import pe.gob.onp.thaqhiri.repository.VisitPlanRepository;
import pe.gob.onp.thaqhiri.repository.UserRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.UCoordenadas;
import pe.gob.onp.thaqhiri.util.UFecha;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class VisitPlanImportService {

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 18:05 UTC-5 (Lima)][desc: Cambia el formato de fecha de import/export de planes a dd/MM/yyyy (mantiene compatibilidad con yyyy-MM-dd)][obj: VisitPlanImportService]
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FMT_ISO = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneOffset LIMA_OFFSET = ZoneOffset.ofHours(-5);

    private final UserService userService;
    private final DestinoRepository destinoRepository;
    private final VisitPlanRepository planRepository;
    private final UserRepository userRepository;
    private final VisitPlanService visitPlanService;
    private final DestinoService destinoService;
    private final MapboxGeocodingService mapboxGeocodingService;
    private record ColaboradorFechaKey(String colaboradorRaw, String fechaRaw) {}

    public VisitPlanImportService(
            UserService userService,
            DestinoRepository destinoRepository,
            VisitPlanRepository planRepository,
            VisitPlanService visitPlanService,
            MapboxGeocodingService mapboxGeocodingService,
            DestinoService destinoService,
            UserRepository userRepository
    ) {
        this.userService = userService;
        this.destinoRepository = destinoRepository;
        this.planRepository = planRepository;
        this.visitPlanService = visitPlanService;
        this.mapboxGeocodingService = mapboxGeocodingService;
        this.destinoService = destinoService;
        this.userRepository= userRepository;
    }

    
    private CellStyle generarEstiloTitulo(XSSFWorkbook workbook) {
    	
    	
    	// Estilo para el titulo (fondo sin color y sin bordes)
    	CellStyle tituloStyle = workbook.createCellStyle();
    	
    	// Fuente en negrita
    	Font tituloFont = workbook.createFont();
    	tituloFont.setFontHeight((short) 10);
    	tituloFont.setBold(true);
    	tituloFont.setFontHeightInPoints((short) 14); // tamaño correcto y visible
    	tituloStyle.setFont(tituloFont);

    	// Alineación
    	tituloStyle.setAlignment(HorizontalAlignment.CENTER);
    	tituloStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    	
    	return tituloStyle;
    }
    
    private CellStyle generarEstiloCabecera(XSSFWorkbook workbook) {
    	
    	
    	// Estilo para encabezado (fondo gris claro + bordes)
    	CellStyle headerStyle = workbook.createCellStyle();

    	// Fondo gris claro
    	headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    	headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    	// Bordes
    	headerStyle.setBorderTop(BorderStyle.THIN);
    	headerStyle.setBorderBottom(BorderStyle.THIN);
    	headerStyle.setBorderLeft(BorderStyle.THIN);
    	headerStyle.setBorderRight(BorderStyle.THIN);

    	// Fuente en negrita
    	Font headerFont = workbook.createFont();
    	headerFont.setBold(true);
    	headerStyle.setFont(headerFont);

    	// Alineación
    	headerStyle.setAlignment(HorizontalAlignment.CENTER);
    	headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    	
    	return headerStyle;
    }
    
    public byte[] generarPlantilla() {
    	
        try (var workbook = new XSSFWorkbook()) {
        	
            var sheet = workbook.createSheet("Planes");
            var cat = workbook.createSheet("_catalogos");
            int row = 0;
            
            //Crear el estilo para la primera fila titulo
            CellStyle tituloStyle = generarEstiloTitulo(workbook);
            
            // Crear la primera fila titulo                        
            var filaTitulo = sheet.createRow(row++);
            Cell celda = filaTitulo.createCell(0);
            celda.setCellValue("Plantilla para la carga masiva de planes de visita");
            celda.setCellStyle(tituloStyle);
            filaTitulo.setHeightInPoints(22);
            
            // Combinar las primeras 7 columnas (0 a 6)
            sheet.addMergedRegion(new CellRangeAddress(
                    0, // fila inicial
                    0, // fila final
                    0, // columna inicial
                    6  // columna final
            ));
            
            //Crear el estilo para la primera fila de encabezados de columna
            CellStyle headerStyle = generarEstiloCabecera(workbook);
            
            // Crear la primera fila de encabezados de columna
            var header = sheet.createRow(row++);
            
            celda = header.createCell(0);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_COLABORADOR);
            celda.setCellStyle(headerStyle);
            
            celda = header.createCell(1);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_FECHA_PLAN);
            celda.setCellStyle(headerStyle);
            
            celda = header.createCell(2);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DESTINO);
            celda.setCellStyle(headerStyle);
            
            celda = header.createCell(3);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DIRECCION);
            celda.setCellStyle(headerStyle);
            
            celda = header.createCell(4);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_HORA_CITA);
            celda.setCellStyle(headerStyle);
            
            celda = header.createCell(5);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PRIORIDAD);
            celda.setCellStyle(headerStyle);
            
            celda = header.createCell(6);            
            celda.setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PV);
            celda.setCellStyle(headerStyle);

            //Ancho de las columnas
            sheet.setColumnWidth(0, 9000); //Colaborador
            sheet.setColumnWidth(1, 6000); //Fecha
            sheet.setColumnWidth(2, 7000); //Destino
            sheet.setColumnWidth(3, 7000); //Direccion
            sheet.setColumnWidth(4, 5000); //HoraCita
            sheet.setColumnWidth(5, 9000); //Prioridad
            sheet.setColumnWidth(6, 3000); //PV
            
            // Crear la hoja de Catálogos (hoja oculta)
            int vr = 0;
            cat.createRow(vr++).createCell(0).setCellValue("VERIFICADORES");
            cat.createRow(vr++).createCell(0).setCellValue("id");
            cat.getRow(1).createCell(1).setCellValue("nombre");
            cat.getRow(1).createCell(2).setCellValue("selector");

            List<User> colaboradores = userService.listarPersonasActivasCampo().stream()
                    .map(dto -> userService.getEntity(dto.id()))
                    .toList();

            int verStart = 2;
            int verRow = verStart;
            for (User u : colaboradores) {
                var r = cat.createRow(verRow++);
                r.createCell(0).setCellValue(u.getId() != null ? u.getId() : 0);
                r.createCell(1).setCellValue(u.getNombre() != null ? u.getNombre() : "");
                r.createCell(2).setCellValue((u.getId() != null ? u.getId() : 0) + " - " + (u.getNombre() != null ? u.getNombre() : ""));
            }

            int dr = verRow + 2;
            cat.createRow(dr++).createCell(0).setCellValue("DESTINOS");
            cat.createRow(dr++).createCell(0).setCellValue("codigo");
            cat.getRow(dr - 1).createCell(1).setCellValue("nombre");
            cat.getRow(dr - 1).createCell(2).setCellValue("selector");

            int destStart = dr;
            int destRow = destStart;
            for (Destino d : destinoRepository.findByEstadoRegistroOrderByNombreAsc(UConstante.ACTIVO_REGI)) {
                var r = cat.createRow(destRow++);
                r.createCell(0).setCellValue(d.getNombre() != null ? d.getNombre() : "");
                r.createCell(1).setCellValue(d.getNombre() != null ? d.getNombre() : "");
            }

            // Rangos con nombre
            createNamedRange(workbook, "VERIFICADORES_LIST", cat.getSheetName(), verStart, verRow - 1, 2);

            // Validaciones (hasta 2000 filas)
            
            //Validacion del colaborador
            addListValidation(sheet, 2, 2000, 0, 0, "VERIFICADORES_LIST");
            
            //Validacion de la prioridad
            addExplicitListValidation(sheet, 2, 2000, 5, 5, new String[] {"MUY_ALTA", "ALTA", "NORMAL"});

            //validación de fecha en plantilla de planes a dd/MM/yyyy][obj: VisitPlanImportService.generarPlantilla]
            addDateValidation(sheet, 2, 2000, 1, 1);
            
            //Estilo para la fecha del plan
            applyDateColumnStyle(workbook, sheet, 1);
            
            // Validación de hora (HH:mm)
            addTimeValidation(sheet, 2, 2000, 4, 4);

            // Formato visual
            applyTimeColumnStyle(workbook, sheet, 4);
            workbook.setSheetHidden(workbook.getSheetIndex(cat), true);

            var out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } 
        catch (Exception ex) {
            throw new BusinessException("No se pudo generar la plantilla de Planes");
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:20 UTC-5 (Lima)][desc: Genera Excel de export de planes existentes (una fila por visita) con las mismas validaciones de la plantilla][obj: VisitPlanImportService.exportarExcel]
    @Transactional(readOnly = true)
    public byte[] exportarExcel(String idPersona, LocalDate fechaPlan) {
        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Planes");
            var cat = workbook.createSheet("_catalogos");

            // Header
            int row = 0;
            var header = sheet.createRow(row++);
            header.createCell(0).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_COLABORADOR);
            header.createCell(1).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_FECHA_PLAN);
            header.createCell(2).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_ORDEN);
            header.createCell(3).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DESTINO);
            header.createCell(4).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_HORA_CITA);
            header.createCell(5).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PRIORIDAD);
            header.createCell(6).setCellValue(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PV);

            // Catálogos
            buildCatalogs(workbook, sheet, cat);
            
            //Pasar la lista de ids de personas a una coleccion
    		Collection<Long> ids = null;

    	    if (idPersona != null && !idPersona.isBlank()) {
    	        ids = Arrays.stream(idPersona.split(","))
    	                .map(String::trim)
    	                .map(Long::valueOf)
    	                .toList();
    	    }

            // Datos: reutiliza búsqueda del repositorio
            var plansPage = planRepository.buscarPlanes(ids, fechaPlan, org.springframework.data.domain.Pageable.unpaged());
            for (var plan : plansPage.getContent()) {
                var verifier = plan.getVerifier();
                String verifierSel = (verifier != null && verifier.getId() != null)
                        ? (verifier.getId() + " - " + (verifier.getNombre() != null ? verifier.getNombre() : ""))
                        : "";
                LocalDate plannedFor = plan.getPlannedFor();

                // items activos
                var items = visitPlanService.getItemsForExport(plan.getId());
                for (var item : items) {
                    var r = sheet.createRow(row++);
                    r.createCell(0).setCellValue(verifierSel);

                    Cell dateCell = r.createCell(1);
                    if (plannedFor != null) {
                        dateCell.setCellValue(java.util.Date.from(plannedFor.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    } else {
                        dateCell.setBlank();
                    }

                    r.createCell(2).setCellValue(item.orderIndex() != null ? item.orderIndex() : 0);
                    
                    r.createCell(3).setCellValue(item.companyName());

                    String hhmm = null;
                    if (item.targetTime() != null) {
                        hhmm = item.targetTime().withOffsetSameInstant(LIMA_OFFSET).toLocalTime().format(TIME_FMT);
                    }
                    r.createCell(4).setCellValue(hhmm != null ? hhmm : "");
                    r.createCell(5).setCellValue(item.prioridad() != null ? item.prioridad() : "NORMAL");
                    r.createCell(6).setCellValue(item.plantillaPv() != null ? item.plantillaPv() : "");
                }
            }

            for (int i = 0; i <= 6; i++) sheet.autoSizeColumn(i);
            workbook.setSheetHidden(workbook.getSheetIndex(cat), true);

            var out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException("No se pudo generar el Excel de export de planes");
        }
    }

    private void buildCatalogs(XSSFWorkbook workbook, Sheet sheet, Sheet cat) {
        int row = 0;
        // Colaboradores
        cat.createRow(row++).createCell(0).setCellValue("VERIFICADORES");
        cat.createRow(row++).createCell(0).setCellValue("id");
        cat.getRow(1).createCell(1).setCellValue("nombre");
        cat.getRow(1).createCell(2).setCellValue("selector");

        List<User> colaboradores = userService.listarPersonasActivasCampo().stream()
                .map(dto -> userService.getEntity(dto.id()))
                .toList();

        int verStart = row;
        for (User u : colaboradores) {
            var r = cat.createRow(row++);
            r.createCell(0).setCellValue(u.getId() != null ? u.getId() : 0);
            r.createCell(1).setCellValue(u.getNombre() != null ? u.getNombre() : "");
            r.createCell(2).setCellValue((u.getId() != null ? u.getId() : 0) + " - " + (u.getNombre() != null ? u.getNombre() : ""));
        }
        int verEnd = row - 1;

        row += 2;
        // Destinos
        cat.createRow(row++).createCell(0).setCellValue("DESTINOS");
        cat.createRow(row++).createCell(0).setCellValue("codigo");
        cat.getRow(row - 1).createCell(1).setCellValue("nombre");
        cat.getRow(row - 1).createCell(2).setCellValue("selector");

        int destStart = row;
        for (Destino d : destinoRepository.findByEstadoRegistroOrderByNombreAsc(UConstante.ACTIVO_REGI)) {
            var r = cat.createRow(row++);
            r.createCell(0).setCellValue(d.getNombre() != null ? d.getNombre() : "");
            r.createCell(1).setCellValue(d.getNombre() != null ? d.getNombre() : "");
        }
        int destEnd = row - 1;

        createNamedRange(workbook, "VERIFICADORES_LIST", cat.getSheetName(), verStart, verEnd, 2);
        createNamedRange(workbook, "DESTINOS_LIST", cat.getSheetName(), destStart, destEnd, 2);

        addListValidation(sheet, 2, 5000, 0, 0, "VERIFICADORES_LIST");
        addListValidation(sheet, 2, 5000, 3, 3, "DESTINOS_LIST");
        addExplicitListValidation(sheet, 2, 5000, 5, 5, new String[] {"MUY_ALTA", "ALTA", "NORMAL"});
        addDateValidation(sheet, 2, 5000, 1, 1);
        applyDateColumnStyle(workbook, sheet, 1);
    }

    private void addDateValidation(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        DataValidationHelper helper = new XSSFDataValidationHelper((org.apache.poi.xssf.usermodel.XSSFSheet) sheet);
        // Excel date formulas
        DataValidationConstraint constraint = helper.createDateConstraint(
                DataValidationConstraint.OperatorType.BETWEEN,
                "DATE(2020,1,1)",
                "DATE(2100,12,31)",
                "dd/mm/yyyy"
        );
        CellRangeAddressList range = new CellRangeAddressList(firstRow - 1, lastRow - 1, firstCol, lastCol);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setShowErrorBox(true);
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 18:05 UTC-5 (Lima)][desc: Ajusta el mensaje de validación de fecha en Excel a dd/MM/yyyy][obj: VisitPlanImportService.addDateValidation]
        validation.createPromptBox("Fecha", "Ingrese una fecha válida con formato dd/MM/yyyy.");
        validation.setShowPromptBox(true);
        validation.createErrorBox("Fecha inválida", "Ingrese una fecha válida (dd/MM/yyyy).");
        sheet.addValidationData(validation);
    }

    private void applyDateColumnStyle(XSSFWorkbook workbook, Sheet sheet, int col) {
        CellStyle style = workbook.createCellStyle();
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 18:05 UTC-5 (Lima)][desc: Ajusta el formato visual de la columna FechaPlan en Excel a dd/MM/yyyy][obj: VisitPlanImportService.applyDateColumnStyle]
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        sheet.setDefaultColumnStyle(col, style);
    }

    

    private List<VisitItemCreateRequest> toItemRequests(List<RowItem> items, LocalDate plannedFor) {
        List<VisitItemCreateRequest> list = new ArrayList<>();
        for (RowItem i : items) {
            OffsetDateTime targetTime = null;
            if (i.hora != null) {
                targetTime = OffsetDateTime.of(plannedFor, i.hora, LIMA_OFFSET);
            }

            list.add(new VisitItemCreateRequest(
                    null,
                    i.destino,
                    targetTime,
                    i.direccion,
                    i.prioridad,
                    i.plantillaPv,
                    null
            ));
        }
        return list;
    }

    private int findHeaderRow(Sheet sheet) {
        int first = sheet.getFirstRowNum();
        int last = Math.min(sheet.getLastRowNum(), first + 15);
        
        for (int r = first; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            
            for (Cell c : row) {
            	String v = readCellAsString(row, c.getColumnIndex());
            	
                if (UConstante.XLS_PLAN_NOMBRE_COLUMNA_COLABORADOR.equals(v)) {
                    return r;
                }
            }
        }
        return -1;
    }

    private Map<String, Integer> readHeader(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;
        for (Cell c : headerRow) {
        	String v = readCellAsString(headerRow, c.getColumnIndex());
        	
            if (v == null) continue;
            map.put(v, c.getColumnIndex());
        }
        // defaults by position
        map.putIfAbsent("colaborador", 0);
        map.putIfAbsent("fechaplan", 1);
        map.putIfAbsent("orden", 2);
        map.putIfAbsent("destino", 3);
        map.putIfAbsent("horacita", 4);
        map.putIfAbsent("prioridad", 5);
        map.putIfAbsent("plantillapv", 6);
        return map;
    }

    private String readCellAsString(Row row, Integer idx) {
        if (row == null || idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            /*
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? DATE_FMT.format(cell.getLocalDateTimeCellValue().toLocalDate())
                    : String.valueOf((long) cell.getNumericCellValue());
            */
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    String fmt = cell.getCellStyle().getDataFormatString();

                    // Hora (HH:mm)
                    if (fmt != null && fmt.toLowerCase().contains("h")) {
                        yield cell.getLocalDateTimeCellValue()
                                   .toLocalTime()
                                   .format(TIME_FMT);
                    }

                    // Fecha (dd/MM/yyyy)
                    yield DATE_FMT.format(
                            cell.getLocalDateTimeCellValue().toLocalDate()
                    );
                }

                yield String.valueOf((long) cell.getNumericCellValue());
            }           
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private void createNamedRange(Workbook workbook, String name, String sheetName, int startRow, int endRow, int col) {
        Name n = workbook.createName();
        n.setNameName(name);
        n.setRefersToFormula("'" + sheetName + "'!$" + colToLetter(col) + "$" + (startRow + 1) + ":$" + colToLetter(col) + "$" + (endRow + 1));
    }

    private void addListValidation(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, String namedRange) {
        DataValidationHelper helper = new XSSFDataValidationHelper((org.apache.poi.xssf.usermodel.XSSFSheet) sheet);
        DataValidationConstraint constraint = helper.createFormulaListConstraint(namedRange);
        CellRangeAddressList range = new CellRangeAddressList(firstRow - 1, lastRow - 1, firstCol, lastCol);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private void addExplicitListValidation(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, String[] values) {
        DataValidationHelper helper = new XSSFDataValidationHelper((org.apache.poi.xssf.usermodel.XSSFSheet) sheet);
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        CellRangeAddressList range = new CellRangeAddressList(firstRow - 1, lastRow - 1, firstCol, lastCol);
        DataValidation validation = helper.createValidation(constraint, range);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private String colToLetter(int col) {
        return String.valueOf((char) ('A' + col));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Long parseVerifierId(String raw) {
        if (isBlank(raw)) return null;
        String s = raw.trim();
        int idx = s.indexOf('-');
        if (idx > 0) {
            s = s.substring(0, idx).trim();
        }
        try {
            return Long.parseLong(s);
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer parseInt(String raw) {
        if (isBlank(raw)) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDate parseDate(String raw) {
        if (isBlank(raw)) return null;
        String value = raw.trim();
        try {
            return LocalDate.parse(value, DATE_FMT);
        } catch (Exception ignore) {
            // compat: plantillas antiguas o valores ingresados manualmente en yyyy-MM-dd
            try {
                return LocalDate.parse(value, DATE_FMT_ISO);
            } catch (Exception ignore2) {
                return null;
            }
        }
    }

    private LocalTime parseTime(String raw) {
        if (isBlank(raw)) return null;
        try {
            return LocalTime.parse(raw.trim(), TIME_FMT);
        } catch (Exception ex) {
            return null;
        }
    }

    private String parseDestinoCodigo(String raw) {
        if (isBlank(raw)) return null;
        String s = raw.trim();
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2025-12-24 16:11 UTC-5 (Lima)][desc: No truncar códigos con guion (ej: ONP-013251); separar solo por ' - ' del selector][obj: VisitPlanImportService.parseDestinoCodigo]
        int idx = s.indexOf(" - ");
        if (idx > 0) {
            return s.substring(0, idx).trim();
        }
        return s.trim();
    }

    private String normalizePriority(String raw) {
        if (isBlank(raw)) return "NORMAL";
        String v = raw.trim().toUpperCase();
        if (v.equals("MUY_ALTA") || v.equals("ALTA") || v.equals("NORMAL")) return v;
        return "NORMAL";
    }

    private boolean hasDuplicateOrder(List<RowItem> items) {
        Set<Integer> seen = new HashSet<>();
        for (RowItem i : items) {
            if (!seen.add(i.order)) return true;
        }
        return false;
    }

    private record PlanKey(Long verifierId, LocalDate plannedFor) {}

    //private record RowItem(int order, Destino destino, LocalTime hora, String prioridad, String plantillaPv, int filaExcel) {}
    private record RowItem(int order, String destino, String direccion, LocalTime hora, String prioridad, String plantillaPv, int filaExcel) {}

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return DATE_FMT.format(date);
    }

    private String getVerifierLabel(Long verifierId) {
        if (verifierId == null) return "";
        try {
            User u = userService.getEntity(verifierId);
            String nombre = u != null ? normalizeNullable(u.getNombre()) : null;
            if (nombre != null) return nombre;
        } catch (Exception ignore) {
            // ignore
        }
        return String.valueOf(verifierId);
    }
    
    
    private void addTimeValidation(Sheet sheet,
            int firstRow,
            int lastRow,
            int firstCol,
            int lastCol) 
    {

		DataValidationHelper helper =
		new XSSFDataValidationHelper((org.apache.poi.xssf.usermodel.XSSFSheet) sheet);
		
		// Validación de hora entre 00:00 y 23:59
		DataValidationConstraint constraint = helper.createTimeConstraint(
																		DataValidationConstraint.OperatorType.BETWEEN,
																		"TIME(0,0,0)",
																		"TIME(23,59,0)"
																		);
		
		CellRangeAddressList range =
		new CellRangeAddressList(firstRow - 1, lastRow - 1, firstCol, lastCol);
		
		DataValidation validation = helper.createValidation(constraint, range);
		
		validation.setShowErrorBox(true);
		validation.setShowPromptBox(true);
		
		validation.createPromptBox(
		"Hora",
		"Ingrese una hora válida en formato 24 horas (HH:mm). Ejemplo: 09:30 o 17:45."
		);
		
		validation.createErrorBox(
		"Hora inválida",
		"La hora debe estar en formato HH:mm y entre 00:00 y 23:59."
		);
		
		sheet.addValidationData(validation);
   }
    
    
    private void applyTimeColumnStyle(XSSFWorkbook workbook, Sheet sheet, int col) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(
                workbook.getCreationHelper()
                        .createDataFormat()
                        .getFormat("HH:mm")
        );
        sheet.setDefaultColumnStyle(col, style);
    }
    
    
    /**
     * Metodo que lee el archivo Excel de carga masiva, lo valida y convierte a objetos para mostrarlo en el front. 
     * @param file
     * @param usuarioSesion
     * @param terminalSesion
     * @return
     */
    public VisitPlanValidaResultDTO importExcel(MultipartFile file, String usuarioSesion, String terminalSesion) {
    	
        VisitPlanValidaResultDTO respuesta = new VisitPlanValidaResultDTO();
        ResultadoValidacion resultadoValidacionFinal = new ResultadoValidacion();
        
        try {
        
	        //Validaciones generales del archivo
	        ResultadoValidacion resultadoValidacionGeneral = validarGeneralExcel(file);
	        
	        if (resultadoValidacionGeneral.getTipoResultado() == TipoResultadoValidacion.CON_ERRORES) {
	        	respuesta.setResultadoValidacion(resultadoValidacionGeneral);
	            return respuesta;
	        }
	        
	        //Validaciones por cada fila del archivo
	        ResultadoValidacion resultadoValidacionDetalle = validarDetalleExcel(file);
	        
	        if (resultadoValidacionDetalle.getTipoResultado() == TipoResultadoValidacion.CON_ERRORES) {
	        	respuesta.setResultadoValidacion(resultadoValidacionDetalle);
	            return respuesta;
	        }
	
	        //Obtener los datos y pasar al modelo
	        List<VisitItemMasivo> listaItemsExcel = obtenerItemsExcel(file, usuarioSesion, terminalSesion);
	        respuesta.setListaItemsExcel(listaItemsExcel);

        }
        catch(Exception e) {
        	resultadoValidacionFinal.setTipoResultado(TipoResultadoValidacion.CON_ERRORES);
        	resultadoValidacionFinal.setMensaje("Error al procesar el archivo.");
            respuesta.setResultadoValidacion(resultadoValidacionFinal);        
            return respuesta;
        }
        
        //Si llega a este punto no hay errores
        resultadoValidacionFinal.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);        
        respuesta.setResultadoValidacion(resultadoValidacionFinal);        
        return respuesta;
    }
    
    
    public ResultadoValidacion validarGeneralExcel(MultipartFile file) {
    	
    	ResultadoValidacion resultado = new ResultadoValidacion();
    	resultado.setTipoResultado(TipoResultadoValidacion.CON_ERRORES); //Valor predeterminado
        
        //validaciones generales del archivo
        
        if (file == null || file.isEmpty()) {
        	resultado.setMensaje("Archivo vacío.");
            return resultado;
        }

        Map<PlanKey, List<RowItem>> grouped = new LinkedHashMap<>();

        try (InputStream in = file.getInputStream(); var workbook = WorkbookFactory.create(in)) 
        {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet == null) {                
                resultado.setMensaje("No se encontró la primera hoja.");
                return resultado;
            }
            
            if (sheet.getSheetName() == null) {
                resultado.setMensaje("No se encontró el nombre de la primera hoja.");
                return resultado;
            }
            
            if (!sheet.getSheetName().equals("Planes")) {
                resultado.setMensaje("No se encontró la hoja de nombre: 'Planes'.");
                return resultado;
            }            

            int headerRowIndex = findHeaderRow(sheet);
            if (headerRowIndex < 0) {
                resultado.setMensaje("No se encontró la cabecera de la plantilla.");
                return resultado;
            }

            int last = sheet.getLastRowNum();
            
            if (last < 2) {
                resultado.setMensaje("El archivo no tiene filas de datos.");
                return resultado;
            }
        }
        catch (Exception ex) {
            resultado.setMensaje("No se pudo leer el archivo.");
            return resultado;
        }

        //Si llega a este punto no hay errores
        resultado.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);
        
        return resultado;
    }
    
    
    public ResultadoValidacion validarDetalleExcel(MultipartFile file) {
    	
    	ResultadoValidacion resultado = new ResultadoValidacion();
    	resultado.setTipoResultado(TipoResultadoValidacion.CON_ERRORES); //Valor predeterminado
    	
    	//Validaciones por cada fila del archivo
        try (InputStream in = file.getInputStream(); var workbook = WorkbookFactory.create(in)) 
        {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findHeaderRow(sheet);
            Map<String, Integer> header = readHeader(sheet.getRow(headerRowIndex));
            int last = sheet.getLastRowNum();            
            
            //Para validar que no existan filas duplicadas en el Excel
            Set<String> filasSinDuplicadosSet = new HashSet<>();
            
            //Para validar que el plan para el colaborador y fecha no exista.            
            Set<ColaboradorFechaKey> colaboradorFechaSet = new HashSet<>();
            
            for (int r = headerRowIndex + 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String colaboradorRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_COLABORADOR));
                String fechaRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_FECHA_PLAN));
                String destinoRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DESTINO));
                String direccionRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DIRECCION));
                String horaRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_HORA_CITA));
                String prioRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PRIORIDAD));
                String plantillaRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PV));

                if (isBlank(colaboradorRaw) && isBlank(fechaRaw) && isBlank(destinoRaw)) {
                    continue;
                }

                Long verifierId = parseVerifierId(colaboradorRaw);
                if (verifierId == null) {
                    resultado.setMensaje("Seleccione un colaborador en la fila " + (r+1) + ".");
                    return resultado;
                }

                LocalDate plannedFor = parseDate(fechaRaw);
                if (plannedFor == null) {
                    resultado.setMensaje("Fecha inválida en la fila " + (r+1) + ", Use dd/MM/yyyy.");
                    return resultado;
                }
                
                LocalDate hoy = LocalDate.now();
                if (plannedFor.isBefore(hoy)) {
                    resultado.setMensaje("La fecha planificada no puede ser menor a la fecha actual en la fila " + (r + 1) + ".");
                    return resultado;
                }

                if (isBlank(destinoRaw)) {
                    resultado.setMensaje("Ingrese el destino en la fila " + (r+1) + ".");
                    return resultado;
                }
                
                if (isBlank(direccionRaw)) {
                    resultado.setMensaje("Ingrese la dirección en la fila " + (r+1) + ".");
                    return resultado;
                }
                
                //FALTA: validar la hora 
                LocalTime hora = parseTime(horaRaw);
                
                String prioridad = normalizePriority(prioRaw);
                if(prioridad == null || prioridad.trim().length() == 0) {
	                resultado.setMensaje("Seleccione la prioridad en la fila " + (r+1) + ".");
	                return resultado;
                }
                
                //La prioridad seleccionada debe ser valida
                TipoPrioridad prioridadEnum;

                try {
                    prioridadEnum = TipoPrioridad.valueOf(prioridad);
                    
                    if(prioridadEnum != TipoPrioridad.MUY_ALTA && prioridadEnum != TipoPrioridad.ALTA && prioridadEnum != TipoPrioridad.NORMAL)                        
                    {
    	                resultado.setMensaje("Seleccione la prioridad en la fila " + (r+1) + ".");
    	                return resultado;
                    }
                } catch (IllegalArgumentException | NullPointerException e) {
                    resultado.setMensaje("Seleccione la prioridad en la fila " + (r + 1) + ".");
                    return resultado;
                }                
                
                String plantilla = normalizeNullable(plantillaRaw);                
                if(plantilla == null || plantilla.trim().length() == 0) {
	                resultado.setMensaje("Ingrese el código de plantilla en la fila " + (r+1) + ".");
	                return resultado;
                }
                
                //Validar que no existan filas duplicadas (No debe repetirse: colaborador, fecha, destino, direccion) 
                String claveFila = verifierId + "|" + fechaRaw + "|" + destinoRaw + "|" + direccionRaw;

                if (!filasSinDuplicadosSet.add(claveFila)) {
                    resultado.setMensaje("La fila " + (r + 1) + " tiene datos duplicados (Colaborador, fecha, destino, dirección).");
                    return resultado;
                }
                
                //Armar la coleccion de PK de planes para validar despues                
                ColaboradorFechaKey key = new ColaboradorFechaKey(colaboradorRaw, fechaRaw);
                colaboradorFechaSet.add(key);                
            }
            
            //Validar en la base de datos que los planes por verifierId y plannedFor no existan en la BD
            resultado = validarPlanExiste(colaboradorFechaSet);
            return resultado;
        } 
        catch (Exception ex) {
        	resultado.setMensaje("No se pudo leer el archivo.");
            return resultado;
        }
    }
    
    
    public List<VisitItemMasivo> obtenerItemsExcel(MultipartFile file, String usuarioSesion, String terminalSesion) throws Exception {
    	
    	List<VisitItemMasivo> listaItemsExcel = new ArrayList<VisitItemMasivo>();
    	
        //Obtener los datos y pasar al modelo
        try (InputStream in = file.getInputStream(); var workbook = WorkbookFactory.create(in)) 
        {
            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findHeaderRow(sheet);
            Map<String, Integer> header = readHeader(sheet.getRow(headerRowIndex));
            int last = sheet.getLastRowNum();            
            
            for (int r = headerRowIndex + 1; r <= last; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String colaboradorRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_COLABORADOR));
                Long verifierId = parseVerifierId(colaboradorRaw);
                
                String fechaRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_FECHA_PLAN));
                String destinoRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DESTINO));
                String direccionRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_DIRECCION));
                String horaRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_HORA_CITA));
                String prioRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PRIORIDAD));
                String plantillaRaw = readCellAsString(row, header.get(UConstante.XLS_PLAN_NOMBRE_COLUMNA_PV));

                if (isBlank(colaboradorRaw) && isBlank(fechaRaw) && isBlank(destinoRaw)) {
                    continue;
                }
                
                //Obtener los datos
                VisitItemMasivo itemExcel = new VisitItemMasivo();
                itemExcel.setColaboradorId(verifierId);
                itemExcel.setFecha(fechaRaw);                
                itemExcel.setHoraCita(horaRaw);
                itemExcel.setPlantillaPv(plantillaRaw);
                itemExcel.setPrioridad(prioRaw);
                itemExcel.setDestinoNombre(destinoRaw);
                itemExcel.setDireccion(direccionRaw);
                
                //Buscar el destino en el catálogo o registrarlo
                DestinoDTO destinoDto = obtenerDestino(destinoRaw, direccionRaw, usuarioSesion, terminalSesion);                
                
            	itemExcel.setDestinoId(destinoDto.getId());
            	itemExcel.setValidado(destinoDto.isValidado());
            	itemExcel.setMensajeValidacion(destinoDto.getMensajeValidacion());
                
                listaItemsExcel.add(itemExcel);
            }
        }
        catch (Exception ex) {        	
            throw(ex);
        }

        return listaItemsExcel;
    }

    
    private DestinoDTO obtenerDestino(String destino, String direccion, String usuarioSesion, String terminalSesion) {
    	
    	DestinoDTO resultado = null;
    	Destino entidad = new Destino();
    	
    	Optional<Destino> destinoOpt =
    			destinoRepository.findFirstByNombreIgnoreCaseAndDireccionIgnoreCaseAndEstadoRegistro(
    					destino,
    					direccion,
    					UConstante.ACTIVO_REGI
    	        );

    	if (destinoOpt.isPresent()) {
    		//Devolver el destino encontrado
    		entidad = destinoOpt.get();
    		resultado = destinoService.toDto(entidad);
    		
    		resultado.setValidado(true); //Ya existe, no requiere validacion
    	}
    	else {
    		System.out.println("obtenerDestino(), se registrará nuevo destino, destino=" + destino + ", direccion=" + direccion);
    		
    		//Registrar el destino como nuevo
    		Destino nuevo = new Destino();
    		nuevo.setNombre(destino);
    		nuevo.setDireccion(direccion);
    		
    		//Obtener las coordenadas
    		var sug = mapboxGeocodingService.forwardGeocode(direccion);
            
    		if (sug.isEmpty()) {
    			//Si no encontró las coordenadas entonces no se puede registrar al destino
    			resultado = new DestinoDTO();
    			resultado.setId(null);
    			resultado.setValidado(false); //Requiere validacion del usuario
    			resultado.setMensajeValidacion("No se encontró la coordenada para la dirección ingresada, busque o registre al destino.");
    		}
    		else {
    			//Cuando encuentra las coordenadas se procede a registrar el destino
            	var s = sug.get();
            	
            	nuevo.setLatitud(s.lat());
        		nuevo.setLongitud(s.lng());
    		
        		nuevo.setCategoria("Entidad estatal");
        		nuevo.setPrecision("APROXIMADO");
        		nuevo.setUsuarioCreacion(usuarioSesion);
        		nuevo.setTerminalCreacion(terminalSesion);
        		nuevo.setEstadoRegistro(UConstante.ACTIVO_REGI);
        		
        		entidad = destinoRepository.save(nuevo);
        		resultado = destinoService.toDto(entidad);
        		
        		resultado.setValidado(false); //Requiere validacion del usuario, por haber registrado recien.
        		resultado.setMensajeValidacion("Se registró el destino, valide en el mapa que la coordenada corresponda a la dirección.");
            }
    	}    	
    	
    	return resultado;
    }
    
    private ResultadoValidacion validarPlanExiste(Set<ColaboradorFechaKey> colaboradorFechaSet) {
    
    	ResultadoValidacion resultado = new ResultadoValidacion();
    	resultado.setTipoResultado(TipoResultadoValidacion.CON_ERRORES); //Valor predeterminado
    	
    	for (ColaboradorFechaKey key : colaboradorFechaSet) {

    	    String colaboradorRaw = key.colaboradorRaw();
    	    Long verifierId = parseVerifierId(colaboradorRaw);
    	    
    	    String fechaRaw = key.fechaRaw();    	    
    	    LocalDate plannedFor = parseDate(fechaRaw);
    	    
    	    boolean existe = planRepository.existsByVerifierIdAndPlannedForAndStRegi(verifierId, plannedFor, UConstante.ACTIVO_REGI);

    	    if (existe) {    	    	
    	        resultado.setMensaje("Ya existe un plan registrado para el colaborador '" + colaboradorRaw + "' en la fecha " + fechaRaw);
    	        return resultado;
    	    }
    	}
    	
    	//Si llega a este punto ningun plan existe.
    	resultado.setTipoResultado(TipoResultadoValidacion.SIN_ERRORES);
    	
    	return resultado;
    }
    
        
    
}
