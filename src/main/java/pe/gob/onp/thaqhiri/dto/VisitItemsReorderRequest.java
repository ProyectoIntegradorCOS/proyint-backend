package pe.gob.onp.thaqhiri.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VisitItemsReorderRequest(
        @NotEmpty(message = "Se requiere la lista de items en nuevo orden")
        List<Long> itemIds
) {
}
