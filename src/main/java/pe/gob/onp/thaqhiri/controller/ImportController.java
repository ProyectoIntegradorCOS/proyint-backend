package pe.gob.onp.thaqhiri.controller;

import java.io.InputStream;
import java.time.Instant;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import pe.gob.onp.thaqhiri.dto.ImportJobStatusDTO;
import pe.gob.onp.thaqhiri.entity.ImportJob;
import pe.gob.onp.thaqhiri.service.DestinoImportService;
import pe.gob.onp.thaqhiri.service.ImportJobService;
import pe.gob.onp.thaqhiri.util.USesion;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final DestinoImportService importService;
    private final ImportJobService jobService;

    public ImportController(DestinoImportService importService,
                            ImportJobService jobService) {
        this.importService = importService;
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<Long> importar(@RequestParam MultipartFile file, HttpServletRequest httpRequest) throws Exception {

    	// ⏱️ Momento de inicio
        Instant inicio = Instant.now();
        
    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        int totalFilas;

        try (InputStream in = file.getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(in)) {

            totalFilas = workbook.getSheetAt(0).getLastRowNum() - 2;
        }
        
        Long jobId = jobService.crearJob(totalFilas, usuarioSesion, terminalSesion);

        byte[] fileBytes = file.getBytes();
        
        importService.importExcelAsync(inicio, fileBytes, file.getOriginalFilename(), usuarioSesion, terminalSesion, jobId);

        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<ImportJobStatusDTO> estado(@PathVariable Long jobId) {
    	
    	ImportJobStatusDTO estado = jobService.obtener(jobId);
    	
        return ResponseEntity.ok(estado);
        
        
    }
    
}
