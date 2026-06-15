package pe.gob.onp.thaqhiri.controller;

import pe.gob.onp.thaqhiri.dto.EquipoDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaBusquedaDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.UserResponse;
import pe.gob.onp.thaqhiri.service.EquipoService;
import pe.gob.onp.thaqhiri.util.UConstante;
import pe.gob.onp.thaqhiri.util.USesion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import pe.gob.onp.thaqhiri.auth.SaaPrincipal;

/**
 * Controlador REST para la gestión de Equipos.
 * Utiliza RespuestaDTO para estandarizar la salida de la API.
 */
// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 11:23 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: EquipoController swagger]
@Tag(name = "Equipos", description = "Gestion de equipos de trabajo")
@RestController
@RequestMapping({"/api/equipo", "/api/equipos"})
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    /**
     * Endpoint para obtener la lista de equipos activos.
     * Mapea a /api/equipo/lista-activa
     * @return ResponseEntity<RespuestaDTO<EquipoDTO>>
     */
    @Operation(summary = "Listar equipos activos", description = "Devuelve la lista de equipos activos")
    @GetMapping("/lista-activa")
    public ResponseEntity<RespuestaDTO<EquipoDTO>> listarActivos() {
        
        RespuestaDTO<EquipoDTO> respuesta;        
    
        // Se asume que equipoService.listarActivos() ahora devuelve List<EquipoDTO>
        List<EquipoDTO> equiposDTO = equipoService.listarActivos();

        if (equiposDTO.size() > 0) {
            respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,"Lista de equipos activa obtenida exitosamente.",equiposDTO
            );
        } else {
            // No se encontraron resultados
            respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,"No se encontraron equipos activos.",List.of() // Retorna una lista vacía
            );
        }
        
        return ResponseEntity.ok(respuesta);
    }
    
    /**
     * Recupera la lista de equipos supervisados por el usuario recorriendo toda la jerarquia de forma recursiva.
     * @param idSupervisor
     * @return
     */
    @Operation(summary = "Listar equipos supervisados", description = "Obtiene equipos supervisados por un supervisor")
    @GetMapping("/supervisados/{idSupervisor}")
    public ResponseEntity<RespuestaDTO<EquipoDTO>>  listarEquiposSupervisadosPorusuario(
            @PathVariable Long idSupervisor) 
    {
    	RespuestaDTO<EquipoDTO> respuesta; 
    	List<EquipoDTO> equiposDTO = equipoService.obtenerEquiposSupervisados(idSupervisor);
    	
    	if (equiposDTO.size() > 0) {
            respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,"Lista de equipos activa obtenida exitosamente.",equiposDTO
            );
        } else {
            // No se encontraron resultados
            respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,"No se encontraron equipos activos.",List.of() // Retorna una lista vacía
            );
        }
    			
        return ResponseEntity.ok(respuesta);
    }
    
    
    /**
     * Realiza la busqueda paginada de equipos.
     * @param nombreEquipo
     * @param nombreSupervisor
     * @param pagina
     * @param tamanioPagina
     * @param orden
     * @param columnaOrden
     * @return
     */
    @Operation(summary = "Buscar equipos", description = "Busca equipos por filtros con paginacion")
    @GetMapping("/buscar")
    public ResponseEntity<RespuestaBusquedaDTO<EquipoDTO>> buscar(
    		@RequestParam(required = false) String nombreEquipo,
    		@RequestParam(required = false) String nombreSupervisor,        		
    		@RequestParam(defaultValue = "1") int pagina,
    		@RequestParam(defaultValue = "10") int tamanioPagina,
    		@RequestParam(defaultValue = "asc") String orden,
    		@RequestParam(defaultValue = "nombre") String columnaOrden) {
    	
        RespuestaBusquedaDTO<EquipoDTO> respuesta = equipoService.buscarPaginado(nombreEquipo, nombreSupervisor, pagina, tamanioPagina, orden, columnaOrden);
    	
    	return ResponseEntity.ok(respuesta);
    }
    
    
    @Operation(summary = "Registrar equipo", description = "Crea un equipo de trabajo")
    @PostMapping("/registrar")
    public ResponseEntity<RespuestaDTO<EquipoDTO>> registrar(@RequestBody EquipoDTO dto,
                                                             @AuthenticationPrincipal SaaPrincipal principal,
                                                             HttpServletRequest httpRequest) {

    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	//Grabar
        EquipoDTO creado = equipoService.registrar(dto, usuarioSesion, terminalSesion);

        //Retornar respuesta
        RespuestaDTO<EquipoDTO> respuesta =
                new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_EXITOSO,
                        "Equipo registrado correctamente.",
                        List.of(creado)
                );

        return ResponseEntity.ok(respuesta);
    }

    @Operation(summary = "Actualizar equipo", description = "Actualiza un equipo existente")
    @PutMapping("/actualizar")
    public ResponseEntity<RespuestaDTO<EquipoDTO>> actualizar(@RequestBody EquipoDTO dto,
                                                              @AuthenticationPrincipal SaaPrincipal principal,
                                                              HttpServletRequest httpRequest) {

    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
    	//Grabar
        EquipoDTO actualizado = equipoService.actualizar(dto, usuarioSesion, terminalSesion);

      //Retornar respuesta
        RespuestaDTO<EquipoDTO> respuesta =
                new RespuestaDTO<>(
                        "" + UConstante.RESULTADO_EXITOSO,
                        "Equipo actualizado correctamente.",
                        List.of(actualizado)
                );

        return ResponseEntity.ok(respuesta);
    }
    
    
    @Operation(summary = "Eliminar equipo", description = "Eliminacion logica de equipo por id")
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<RespuestaDTO<EquipoDTO>> eliminar(@PathVariable Long id,
                                                            @AuthenticationPrincipal SaaPrincipal principal,
                                                            HttpServletRequest httpRequest) {

    	String usuarioSesion = USesion.resolveUsuario();
    	String terminalSesion = USesion.determineHost(httpRequest);
    	
        String msgEliminado = equipoService.eliminar(id, usuarioSesion, terminalSesion);        

        if (msgEliminado == null) {
            return ResponseEntity.ok(new RespuestaDTO<>("0", "Equipo eliminado correctamente.", List.of()));
        }
        else {
        	//No se pudo eliminar
        	return ResponseEntity.ok(new RespuestaDTO<>("1", msgEliminado, List.of()));
        }        
    }

    
    
    @Operation(summary = "Buscar equipos por nombre", description = "Busca equipos por nombre exacto o parcial")
    @GetMapping("/buscar-por-nombre")
    public ResponseEntity<RespuestaDTO<EquipoDTO>>  obtenerEquiposPorNombre(
    		@RequestParam(required = true) String nombreEquipo) 
    {
    	//Valor predeterminado
    	RespuestaDTO<EquipoDTO> respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,"No se encontraron equipos activos.",List.of()); // Retorna una lista vacía
    	
    	if(nombreEquipo != null && nombreEquipo.trim().length() > 0) {	    	 
	    	List<EquipoDTO> equiposDTO = equipoService.buscarPorNombre(nombreEquipo);
	    	
	    	if (equiposDTO.size() > 0) {
	            respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,"Lista de equipos activa obtenida exitosamente.",equiposDTO
	            );
	        }
    	}	
    	
        return ResponseEntity.ok(respuesta);
    }
    
    
    @Operation(summary = "Buscar equipos por nombre excluyendo id", description = "Busca equipos por nombre excluyendo un id")
    @GetMapping("/buscar-por-nombre-otro-id")
    public ResponseEntity<RespuestaDTO<EquipoDTO>>  obtenerEquiposPorNombreOtroId(
    		@RequestParam(required = true) String nombreEquipo,
    		@RequestParam(required = true) Integer idEquipo) 
    {
    	//Valor predeterminado
    	RespuestaDTO<EquipoDTO> respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_NO_ENCONTRO_FILAS,"No se encontraron equipos activos.",List.of()); // Retorna una lista vacía
    	
    	if(nombreEquipo != null && nombreEquipo.trim().length() > 0 &&
    	   idEquipo != null && idEquipo > 0) 
    	{	    	 
	    	List<EquipoDTO> equiposDTO = equipoService.buscarPorNombreOtroId(nombreEquipo, idEquipo);
	    	
	    	if (equiposDTO.size() > 0) {
	            respuesta = new RespuestaDTO<>("" + UConstante.RESULTADO_SI_ENCONTRO_FILAS,"Lista de equipos activa obtenida exitosamente.",equiposDTO
	            );
	        }
    	}	
    	
        return ResponseEntity.ok(respuesta);
    }
    
}
