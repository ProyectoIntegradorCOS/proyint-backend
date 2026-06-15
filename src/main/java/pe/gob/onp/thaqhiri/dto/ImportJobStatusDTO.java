package pe.gob.onp.thaqhiri.dto;

public record ImportJobStatusDTO(
        Long jobId,
        int totalFilas,
        int filasProcesadas,
        int porcentaje,
        String estado,
        String mensaje,
        long horasRestantes,
        long minutosRestantes,
        long segundosRestantes
) {}
