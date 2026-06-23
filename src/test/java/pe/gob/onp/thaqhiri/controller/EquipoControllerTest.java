package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.config.SecurityConfig;
import pe.gob.onp.thaqhiri.dto.EquipoDTO;
import pe.gob.onp.thaqhiri.service.EquipoService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import pe.gob.onp.thaqhiri.config.TestMeterConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EquipoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class
        })
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestMeterConfig.class)
class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarActivos_returnsList() throws Exception {
        var equipo = new EquipoDTO();
        equipo.setId(1);
        equipo.setNombre("Equipo 1");

        Mockito.when(equipoService.listarActivos()).thenReturn(List.of(equipo));

        mockMvc.perform(get("/api/equipo/lista-activa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].nombre").value("Equipo 1"));
    }

    @Test
    void listarActivos_returnsEmpty() throws Exception {
        Mockito.when(equipoService.listarActivos()).thenReturn(List.of());

        mockMvc.perform(get("/api/equipo/lista-activa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados").isEmpty());
    }
}
