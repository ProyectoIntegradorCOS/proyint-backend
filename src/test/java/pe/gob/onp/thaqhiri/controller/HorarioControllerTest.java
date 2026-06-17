package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.dto.HorarioDTO;
import pe.gob.onp.thaqhiri.service.HorarioService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = HorarioController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
class HorarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HorarioService horarioService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_returnsList() throws Exception {
        var horario = new HorarioDTO();
        horario.setId(1L);
        horario.setNombre("Turno Mañana");

        Mockito.when(horarioService.getAll()).thenReturn(List.of(horario));

        mockMvc.perform(get("/api/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Turno Mañana"));
    }

    @Test
    void getById_returnsHorario() throws Exception {
        var horario = new HorarioDTO();
        horario.setId(1L);
        horario.setNombre("Turno Mañana");

        Mockito.when(horarioService.getById(1L)).thenReturn(Optional.of(horario));

        mockMvc.perform(get("/api/horarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Turno Mañana"));
    }

    @Test
    void getById_returnsNotFound() throws Exception {
        Mockito.when(horarioService.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/horarios/99"))
                .andExpect(status().isNotFound());
    }
}
