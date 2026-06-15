package pe.gob.onp.thaqhiri.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import pe.gob.onp.thaqhiri.dto.DestinoDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.entity.Destino;
import pe.gob.onp.thaqhiri.exception.BusinessException;
import pe.gob.onp.thaqhiri.exception.ResourceNotFoundException;
import pe.gob.onp.thaqhiri.repository.DestinoRepository;
import pe.gob.onp.thaqhiri.util.UConstante;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class DestinoService {

    private final DestinoRepository destinoRepository;

    public DestinoService(DestinoRepository destinoRepository) {
        this.destinoRepository = destinoRepository;
    }



    @Transactional(readOnly = true)
    public RespuestaBusquedaDTO<DestinoDTO> buscarPaginado(
            String destino,
            String direccion,
            int pagina,
            int tamanioPagina,
            String orden,
            String columnaOrden,
            String columnaSegundoOrden
    ) {
    	RespuestaBusquedaDTO<DestinoDTO> respuesta = new RespuestaBusquedaDTO<>();
    	
        Sort sort = UConstante.ORDEN_DESCENDENTE.equalsIgnoreCase(orden)
                ? Sort.by(
                		  Sort.Order.desc(columnaOrden),
                          Sort.Order.desc(columnaSegundoOrden)
                        )
                : Sort.by(
                		Sort.Order.asc(columnaOrden),
                        Sort.Order.asc(columnaSegundoOrden)
                		);
        Pageable pageable = PageRequest.of(pagina - 1, tamanioPagina, sort);
        
        Page<Destino> pageResult = destinoRepository.buscarPaginado(normalize(destino), normalize(direccion), pageable);
        long totalRegistros = pageResult.getTotalElements();
        
        Page<DestinoDTO> resultado = pageResult.map(this::toDto);
        
        if (resultado.getContent().isEmpty()) {
            respuesta.setCodigoResultado(UConstante.RESULTADO_NO_ENCONTRO_FILAS);
            respuesta.setMensajeResultado("No se encontraron destinos con los criterios especificados.");
            respuesta.setResultados(List.of());
            respuesta.setTotalPaginas(0);
            respuesta.setTotalRegistros(0);
        } else {
            respuesta.setCodigoResultado(UConstante.RESULTADO_SI_ENCONTRO_FILAS);
            respuesta.setMensajeResultado("Búsqueda exitosa.");
            respuesta.setResultados(resultado.getContent());
            respuesta.setTotalPaginas(resultado.getTotalPages());
            respuesta.setPaginaActual(pagina);
            respuesta.setTamanioPagina(tamanioPagina);
            respuesta.setTotalRegistros(totalRegistros);
        }
        
        return respuesta;
    }

    public DestinoDTO registrar(DestinoDTO dto, String usuario, String terminal) {
        
    	validate(dto, true);

        Destino entity = new Destino();
        apply(dto, entity);
        
        entity.setEstadoRegistro(UConstante.ACTIVO_REGI);
        entity.setUsuarioCreacion(usuario);
        entity.setTerminalCreacion(terminal);
        
        if (entity.getPrecision() == null || entity.getPrecision().isBlank()) {
            entity.setPrecision("APROXIMADO");
        }

        //Grabar en mayusculas 
        entity.setNombre(entity.getNombre().trim().toUpperCase());
        entity.setDireccion(entity.getDireccion().trim().toUpperCase());
        
        Destino saved = destinoRepository.save(entity);
        return toDto(saved);
    }

    public DestinoDTO actualizar(DestinoDTO dto, String usuario, String terminal) {
        
    	validate(dto, false);
        
    	Destino entity = destinoRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Destino no encontrado"));

        apply(dto, entity);
        
        entity.setUsuarioModificacion(usuario);
        entity.setTerminalModificacion(terminal);
        
        //Grabar en mayusculas 
        entity.setNombre(entity.getNombre().trim().toUpperCase());
        entity.setDireccion(entity.getDireccion().trim().toUpperCase());
        
        Destino saved = destinoRepository.save(entity);
        return toDto(saved);
    }

    public String eliminar(Long id, String usuario, String terminal) {
        int updated = destinoRepository.desactivar(id, usuario, terminal);
        
        if (updated == 0) {
            return "Destino no encontrado";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Destino getEntity(Long id) {
        return destinoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destino no encontrado"));
    }

    private void validate(DestinoDTO dto, boolean requireIdNull) {
        if (dto == null) {
            throw new BusinessException("Solicitud inválida");
        }
        if (requireIdNull && dto.getId() != null) {
            throw new BusinessException("El id no debe enviarse al registrar");
        }
        if (!requireIdNull && dto.getId() == null) {
            throw new BusinessException("El id es requerido");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new BusinessException("El nombre es requerido");
        }
        
        /*
        if (dto.getCategoria() == null || dto.getCategoria().trim().isEmpty()) {
            throw new BusinessException("La categoría es requerida");
        }
        */
        
        if (dto.getLatitud() != null && (dto.getLatitud() < -90 || dto.getLatitud() > 90)) {
            throw new BusinessException("Latitud fuera de rango");
        }
        if (dto.getLongitud() != null && (dto.getLongitud() < -180 || dto.getLongitud() > 180)) {
            throw new BusinessException("Longitud fuera de rango");
        }
    }

    private void apply(DestinoDTO dto, Destino entity) {
        entity.setNombre(dto.getNombre().trim());
        
        if(dto.getCategoria() != null) {
        	entity.setCategoria(dto.getCategoria().trim());
        }
        
        //entity.setDireccion(buildFullAddress(dto));
        entity.setDireccion(dto.getDireccion());
        entity.setLatitud(dto.getLatitud());
        entity.setLongitud(dto.getLongitud());
        entity.setReferencia(normalize(dto.getReferencia()));
        entity.setZona(normalize(dto.getZona()));
        entity.setHorarios(normalize(dto.getHorarios()));
        entity.setContacto(normalize(dto.getContacto()));
        entity.setPrecision(normalize(dto.getPrecision()));
        entity.setUbicabilidadOnp(dto.getUbicabilidadOnp());
        entity.setEstadoOnp(dto.getEstadoOnp());
        entity.setFechaActualizacionOnp(dto.getFechaActualizacionOnp());
        
        if (dto.getActivo() != null) {
            entity.setEstadoRegistro(dto.getActivo() ? UConstante.ACTIVO_REGI : UConstante.INACTIVO_REGI);
        }
    }

    public DestinoDTO toDto(Destino entity) {
        DestinoDTO dto = new DestinoDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setCategoria(entity.getCategoria());
        dto.setDireccion(entity.getDireccion());
        dto.setLatitud(entity.getLatitud());
        dto.setLongitud(entity.getLongitud());
        dto.setReferencia(entity.getReferencia());
        dto.setZona(entity.getZona());
        dto.setHorarios(entity.getHorarios());
        dto.setContacto(entity.getContacto());
        dto.setPrecision(entity.getPrecision());
        dto.setUbicabilidadOnp(entity.getUbicabilidadOnp());
        dto.setEstadoOnp(entity.getEstadoOnp());
        dto.setFechaActualizacionOnp(entity.getFechaActualizacionOnp());
        dto.setActivo(UConstante.ACTIVO_REGI.equals(entity.getEstadoRegistro()));

        return dto;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildFullAddress(DestinoDTO dto) {
        String direccion = normalize(dto.getDireccion());
        String distrito = normalize(dto.getDistrito());
        String provincia = normalize(dto.getProvincia());
        String departamento = normalize(dto.getDepartamento());
        StringBuilder sb = new StringBuilder();
        if (direccion != null) sb.append(direccion);
        if (distrito != null) appendPart(sb, distrito);
        if (provincia != null) appendPart(sb, provincia);
        if (departamento != null) appendPart(sb, departamento);
        String out = sb.toString().trim();
        return out.isEmpty() ? null : out;
    }

    private void appendPart(StringBuilder sb, String part) {
        if (part == null || part.isBlank()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(part.trim());
    }


    
    public DestinoDTO obtenerPorId(Long id) {
        return destinoRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }
    
    
    
    public byte[] generarPlantillaDestinos() {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Destinos");

            /* ===============================
             * ESTILOS
             * =============================== */

            // Título
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Header columnas
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            /* ===============================
             * FILA 0 — TÍTULO
             * =============================== */

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(24);

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PLANTILLA PARA LA CARGA MASIVA DE DESTINOS");
            titleCell.setCellStyle(titleStyle);

            // Combinar columnas A y B
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            /* ===============================
             * FILA 1 — VACÍA
             * =============================== */

            sheet.createRow(1);

            /* ===============================
             * FILA 2 — ENCABEZADOS
             * =============================== */

            Row headerRow = sheet.createRow(2);

            Cell c1 = headerRow.createCell(0);
            c1.setCellValue("NOMBRE DEL DESTINO");
            c1.setCellStyle(headerStyle);

            Cell c2 = headerRow.createCell(1);
            c2.setCellValue("DIRECCION");
            c2.setCellStyle(headerStyle);
            
            Cell c3 = headerRow.createCell(2);
            c3.setCellValue("DISTRITO");
            c3.setCellStyle(headerStyle);
            
            Cell c4 = headerRow.createCell(3);
            c4.setCellValue("PROVINCIA");
            c4.setCellStyle(headerStyle);
            
            Cell c5 = headerRow.createCell(4);
            c5.setCellValue("DEPARTAMENTO");
            c5.setCellStyle(headerStyle);
            
            Cell c6 = headerRow.createCell(5);
            c6.setCellValue("UBICABILIDAD");
            c6.setCellStyle(headerStyle);
            
            Cell c7 = headerRow.createCell(6);
            c7.setCellValue("ESTADO ONP");
            c7.setCellStyle(headerStyle);
            
            Cell c8 = headerRow.createCell(7);
            c8.setCellValue("FECHA ACTUALIZACION");
            c8.setCellStyle(headerStyle);

            /* ===============================
             * ANCHOS DE COLUMNA
             * =============================== */

            sheet.setColumnWidth(0, 30 * 256); // Nombre
            sheet.setColumnWidth(1, 40 * 256); // Dirección
            sheet.setColumnWidth(2, 30 * 256); // Distrito
            sheet.setColumnWidth(3, 30 * 256); // Provincia
            sheet.setColumnWidth(4, 30 * 256); // Departamento
            sheet.setColumnWidth(5, 30 * 256); // Ubicabilidad
            sheet.setColumnWidth(6, 30 * 256); // Estado ONP
            sheet.setColumnWidth(7, 30 * 256); // Fecha de actualizacion

            /* ===============================
             * EXPORTAR
             * =============================== */

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando plantilla Excel", e);
        }
    }
    
}
