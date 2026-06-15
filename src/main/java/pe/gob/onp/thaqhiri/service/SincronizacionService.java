package pe.gob.onp.thaqhiri.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
import jakarta.transaction.Transactional;
import pe.gob.onp.thaqhiri.dto.PerfilDTO;
import pe.gob.onp.thaqhiri.dto.RespuestaDTO;
import pe.gob.onp.thaqhiri.dto.SincronizacionDTO;
import pe.gob.onp.thaqhiri.dto.UsuarioPerfilDTO;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pe.gob.onp.thaqhiri.auth.SaaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SincronizacionService {
	private static final Logger log = LoggerFactory.getLogger(SincronizacionService.class);

    private final WebClient webClient;
    private final NamedParameterJdbcTemplate jdbcTemplate; // Para facilitar batches con IN (:ids)
    private final SaaProperties saaProperties;
    
    public SincronizacionService(WebClient webClient, NamedParameterJdbcTemplate jdbcTemplate, SaaProperties saaProperties) {
        this.webClient = webClient;
        this.jdbcTemplate = jdbcTemplate;
        this.saaProperties = saaProperties;
    }

    public List<PerfilDTO> obtenerPerfiles() {
    	
    	String url = this.saaProperties.getPerfilSistemaUrl() + "?idSistema=" + this.saaProperties.getSystemCodeThaqhiri();
    	log.info(url);
        RespuestaDTO<PerfilDTO> response =
                webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<RespuestaDTO<PerfilDTO>>() {})
                        .block();

        if (response == null) {
            throw new IllegalStateException("No se obtuvo respuesta del sistema central para perfiles");
        }

        switch (response.getCodigoResultado()) {
            case "00":
                return response.getResultados() != null ? response.getResultados() : new ArrayList<>();
            case "01":
            	log.info("No se encontraron perfiles en el SAA");
                return new ArrayList<>();
            case "99":
                throw new IllegalStateException("Error del sistema central al obtener perfiles: " + response.getMensajeResultado());
            default:
                throw new IllegalStateException("Código de resultado desconocido al obtener perfiles: " + response.getCodigoResultado());
        }
    }

    public List<UsuarioPerfilDTO> obtenerUsuariosPorPerfil(String noPerfil) {
        String perfilCodificado = UriUtils.encode(noPerfil, StandardCharsets.UTF_8);
        
        String url = this.saaProperties.getUsuarioPerfilVigenteUrl() + "?idSistema=" + this.saaProperties.getSystemCodeThaqhiri() + "&noPerfil=" + perfilCodificado;
        log.info(url);
        RespuestaDTO<UsuarioPerfilDTO> response =
                webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<RespuestaDTO<UsuarioPerfilDTO>>() {})
                        .block();

        if (response == null) {
            throw new IllegalStateException("No se obtuvo respuesta del sistema central para usuarios del perfil " + noPerfil);
        }

        switch (response.getCodigoResultado()) {
            case "00":
                return response.getResultados() != null ? asignarNombrePerfil(response.getResultados(), noPerfil) : new ArrayList<>();
            case "01":
            	log.info("No se encontraron usuarios para el perfil: " + noPerfil);
                return new ArrayList<>();
            case "99":
                throw new IllegalStateException("Error del sistema central al obtener usuarios del perfil " + noPerfil + ": " + response.getMensajeResultado());
            default:
                throw new IllegalStateException("Código de resultado desconocido al obtener usuarios del perfil " + noPerfil + ": " + response.getCodigoResultado());
        }
    }

    
    private List<UsuarioPerfilDTO> asignarNombrePerfil(List<UsuarioPerfilDTO> listaUsuarioPerfilDTO, String nombrePerfil) {
    
    	//Por cada perfil obtener los usuarios que tienen perfil vigente del SAA
        for (UsuarioPerfilDTO usuarioPerfil : listaUsuarioPerfilDTO) {
        	usuarioPerfil.setNombrePerfil(nombrePerfil);
        }
        
        return listaUsuarioPerfilDTO;
    }
    
    
    
    //----------------------------------------------------------------------------------------------------------------------------
    public SincronizacionDTO obtenerDatosSincronizar(String usuarioSesion, String terminalSesion) {
    	
    	SincronizacionDTO objSincronizacion = new SincronizacionDTO();
    	
    	try {    		
    		List<UsuarioPerfilDTO> listaUsuariosNuevosSAACampo = new ArrayList();
    		List<UsuarioPerfilDTO> listaUsuariosReactivarSAACampo = new ArrayList();
    		List<UsuarioPerfilDTO> listaUsuariosSAALocalesActivar = new ArrayList();
    		
    		String perfilCampo = this.saaProperties.getPerfilCampo();
    				
	        //Lista de usuarios vigentes del sistema en el SAA 
	        List<UsuarioPerfilDTO> usuariosSaaActivosSinDuplicados = obtenerUsuariosSaaSinDuplicados();
	        
	        if(usuariosSaaActivosSinDuplicados.size() > 0) {
	        	
		        //Primero, obtener los usuarios nuevos, si existen
		        List<UsuarioPerfilDTO> listaUsuariosNuevosSAA = obtenerUsuariosNuevosSAA(usuariosSaaActivosSinDuplicados);
		        
		        if(listaUsuariosNuevosSAA.size() > 0) {
			        //Separar los usuarios nuevos de campo y no campo
			        listaUsuariosNuevosSAACampo = obtenerUsuariosPorPerfilCampo(listaUsuariosNuevosSAA, perfilCampo);
			        List<UsuarioPerfilDTO> listaUsuariosNuevosSAANoCampo = obtenerUsuariosPorPerfilNoCampo(listaUsuariosNuevosSAA, perfilCampo);
			        
			        //Registrar los usuarios que no son de campo	         
			        registrarUsuariosNuevos(listaUsuariosNuevosSAANoCampo, perfilCampo, usuarioSesion, terminalSesion);
		        }
		        
		        //Segundo, obtener los usuarios que se deben reactivar, si existen
		        listaUsuariosSAALocalesActivar = obtenerIdsLocalesUsuariosActivar(usuariosSaaActivosSinDuplicados);
		        
		        if(listaUsuariosSAALocalesActivar.size() > 0) {
		        	//Separar los usuarios nuevos de campo y no campo
			        listaUsuariosReactivarSAACampo = obtenerUsuariosPorPerfilCampo(listaUsuariosSAALocalesActivar, perfilCampo);
			        List<UsuarioPerfilDTO> listaUsuariosReactivarSAANoCampo = obtenerUsuariosPorPerfilNoCampo(listaUsuariosSAALocalesActivar, perfilCampo);
			        
			        //Activar los usuarios que no son de campo	         
			        activarUsuariosExistentes(listaUsuariosReactivarSAANoCampo, perfilCampo, usuarioSesion, terminalSesion);
		        }
	        }
	        //Devolver los datos	        
	        objSincronizacion.setListaUsuariosNuevosSAA(listaUsuariosNuevosSAACampo);
	        objSincronizacion.setListaUsuariosSAALocalesActivar(listaUsuariosReactivarSAACampo);
    	}
    	catch(Exception e) {
    		log.info("Excepcion: " + e.getMessage());
    		e.printStackTrace();
    		throw(e);
    	}
    	
    	return objSincronizacion;
    }
    
    public List<UsuarioPerfilDTO> obtenerUsuariosSaaSinDuplicados() {
    	//log.info("Entramos a obtenerUsuariosSaaSinDuplicados");
    	List<UsuarioPerfilDTO> usuariosSaaSinDuplicados = new ArrayList<>();
    	
    	try {
    		//Obtener la lista de perfiles del sistema del SAA
        List<PerfilDTO> perfiles = obtenerPerfiles();
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-06 18:18 UTC-5 (Lima)][desc: Loguea detalle de perfiles con for para evitar toString de arrays][obj: SincronizacionService.obtenerUsuariosSaaSinDuplicados perfiles log]
            //log.info("Perfiles total: {}", perfiles.size());
            for (PerfilDTO p : perfiles) {
                if (p == null) continue;
                //log.info("Perfil: idPerfil={} noPerfil={} idSistema={}", p.getIdPerfil(), p.getNoPerfil(), p.getIdSistema());
            }
        Map<Long, UsuarioPerfilDTO> usuariosMap = new HashMap<>();
	
	        //Por cada perfil obtener los usuarios que tienen perfil vigente del SAA
	        for (PerfilDTO perfil : perfiles) {
	            String nombrePerfil = perfil.getNoPerfil();	            
            List<UsuarioPerfilDTO> usuariosPorPerfil = obtenerUsuariosPorPerfil(nombrePerfil);
                // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-06 18:18 UTC-5 (Lima)][desc: Loguea detalle de usuarios por perfil con for para evitar toString de arrays][obj: SincronizacionService.obtenerUsuariosSaaSinDuplicados usuariosPorPerfil log]
                //log.info("Usuarios por Perfil={} total={}", nombrePerfil, usuariosPorPerfil != null ? usuariosPorPerfil.size() : 0);
                if (usuariosPorPerfil != null) {
                    for (UsuarioPerfilDTO u : usuariosPorPerfil) {
                        if (u == null) continue;
                        
                        /*
                        log.info(
                            "UsuarioPerfil: idUsuario={} login={} nombrePerfil={} idEquipo={} idHorario={} idInstitucion={} idRol={} idServicio={}",
                            u.getIdUsuario(),
                            u.getLogin(),
                            u.getNombrePerfil(),
                            u.getIdEquipo(),
                            u.getIdHorario(),
                            u.getIdInstitucion(),
                            u.getIdRol(),
                            u.getIdServicio()
                        );
                        */
                    }
                }
	
	            if(usuariosPorPerfil != null) {
		            //Acumular la lista de usuarios vigentes del SAA sin repetidos
		            for (UsuarioPerfilDTO u : usuariosPorPerfil) {
		            	if(u != null) {
		            		usuariosMap.put(Long.valueOf(u.getIdUsuario()), u);
		            	}
		            }
	            }
                
        }
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-06 18:18 UTC-5 (Lima)][desc: Loguea usuarios únicos con for para evitar toString de mapa][obj: SincronizacionService.obtenerUsuariosSaaSinDuplicados usuariosMap log]
        //log.info("Usuarios Únicos total: {}", usuariosMap.size());
	        
        for (Map.Entry<Long, UsuarioPerfilDTO> entry : usuariosMap.entrySet()) {
            UsuarioPerfilDTO u = entry.getValue();
            if (u == null) continue;
            
            /*
            log.info(
                "UsuarioÚnico: saaId={} login={} nombrePerfil={} idEquipo={} idHorario={}",
                entry.getKey(),
                u.getLogin(),
                u.getNombrePerfil(),
                u.getIdEquipo(),
                u.getIdHorario()
            );
            */
        }
	
	        //Pasar a una lista los usuarios vigentes del sistema en el SAA 
	        usuariosSaaSinDuplicados = new ArrayList<>(usuariosMap.values());
    	}
    	catch(Exception e) {
    		log.info("Excepcion: " + e.getMessage());
    		e.printStackTrace();
    		throw(e);
    	}
    	
    	return usuariosSaaSinDuplicados;
    }
    
    @Transactional
    private List<UsuarioPerfilDTO> obtenerUsuariosNuevosSAA(List<UsuarioPerfilDTO> usuariosActivosSAA) {
        // 1. Recuperar IDs de todos los usuarios locales
        List<Long> idsLocalesSaaSub = jdbcTemplate.getJdbcTemplate()
                .query("SELECT saa_sub FROM thaqhirisys.personal", (rs, rowNum) -> rs.getLong("saa_sub"));

        Set<Long> idsLocalesSaaSubSet = new HashSet<>(idsLocalesSaaSub);

        // 2. Filtrar solo los usuarios nuevos
        List<UsuarioPerfilDTO> nuevosUsuarios = usuariosActivosSAA.stream()
                .filter(u -> !idsLocalesSaaSubSet.contains(Long.valueOf(u.getIdUsuario())))
                .toList();
        
        return nuevosUsuarios;        
    }
    
    
    /**
     * Determina qué usuarios, activos en el sistema SAA, deben ser activados en el sistema local.
     * Los usuarios devueltos son aquellos que están activos en SAA pero actualmente inactivos (ST_REGI = 0) en la tabla 'personal'.
     * @param usuariosActivosSAA
     * @return
     */
    @Transactional
    private List<UsuarioPerfilDTO> obtenerIdsLocalesUsuariosActivar(List<UsuarioPerfilDTO> usuariosActivosSAA) {
        // 1. Recuperar IDs de usuarios locales que están inactivos (ST_REGI = 0)
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 10:51 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: SincronizacionService.obtenerIdsLocalesUsuariosActivar ST_REGI]
        List<Long> idsLocalesInactivos = jdbcTemplate.getJdbcTemplate()
                .query("SELECT saa_sub FROM thaqhirisys.personal WHERE st_regi = '0'", 
                       (rs, rowNum) -> rs.getLong("saa_sub"));

        Set<Long> idsLocalesInactivosSet = new HashSet<>(idsLocalesInactivos);        

        // 2. Determinar IDs que existen en SAA pero están inactivos localmente
        List<UsuarioPerfilDTO> usuariosActivar = usuariosActivosSAA.stream()
                .filter(u -> idsLocalesInactivosSet.contains(Long.valueOf(u.getIdUsuario())))
                .toList();

        return usuariosActivar;
    }
    
    
    
    
    public void sincronizar(List<UsuarioPerfilDTO> listaUsuariosSAA, String usuarioSesion, String terminalSesion) {
    	
    	try {
    		String perfilCampo = this.saaProperties.getPerfilCampo();
    		
    		// 1. Filtrar los usuarios de tipo "NUEVO"
            List<UsuarioPerfilDTO> usuariosNuevos = listaUsuariosSAA.stream()
                .filter(usuario -> "NUEVO".equals(usuario.getTipo()))
                .collect(Collectors.toList());

            // 2. Filtrar los usuarios de tipo "REACTIVAR"
            List<UsuarioPerfilDTO> usuariosReactivar = listaUsuariosSAA.stream()
                .filter(usuario -> "REACTIVAR".equals(usuario.getTipo()))
                .collect(Collectors.toList());
            
            //--- Lógica de sincronización, actualizacion en Thaqhiri
            registrarUsuariosNuevos(usuariosNuevos, perfilCampo, usuarioSesion, terminalSesion);
	        activarUsuariosExistentes(usuariosReactivar, perfilCampo, usuarioSesion, terminalSesion);	        	        
    	}
    	catch(Exception e) {
    		log.info("Excepcion: " + e.getMessage());
    		e.printStackTrace();
    		throw(e);
    	}
    }
    
    
    /**
     * Activa usuarios que están en la lista de SAA pero que están inactivos en la base local
     */
    /**
     * Activa (actualiza st_regi = 1) y asigna Equipo y Horario a usuarios existentes.
     * @param usuariosActivosSaa Lista de usuarios de tipo "REACTIVAR".
     */
    @Transactional
    public void activarUsuariosExistentes(List<UsuarioPerfilDTO> usuariosActivosSaa, String perfilCampo, String usuarioSesion, String terminalSesion) {
        
        if (usuariosActivosSaa.isEmpty()) {
        	log.info("Lista de usuarios a reactivar está vacía. No se ejecutará la actualización.");
            return;
        }

        // --- 1. Definir la sentencia SQL de actualización ---
        final String SQL_UPDATE = "UPDATE thaqhirisys.personal " + 
                                  "SET st_regi = 1, " +               // Activar el estado
                                  "    id_equi = :idEquipo, " +       // Asignar equipo (OPCIONAL)
                                  "    id_hora = :idHorario, " +       // Asignar horario (OBLIGATORIO)
                                  "    no_pers = :noPersona, " +
                                  "    lg_usua = :login, " +
                                  "    ti_trab = :tipoTrabajo, " +
                                  "    id_usua_modi = :usuarioSesion, " + // Auditoría
                                  "    de_term_modi = :terminalSesion " + // Auditoría
                                  "WHERE saa_sub = :saaSubj";           // Filtrar por ID de usuario

        // --- 2. Mapear la lista de DTOs a un array de MapSqlParameterSource ---
        MapSqlParameterSource[] batchParams = usuariosActivosSaa.stream()
            .map(u -> {
                
                // Lógica para manejar idEquipo: Si es NULL o 0 (si usas 0 como valor inválido), se asigna NULL.
                Object idEquipoValor;
                Object idHorarioValor;
                
                if (u.getIdEquipo() < 1) {
                    idEquipoValor = null;
                } else {
                    idEquipoValor = u.getIdEquipo();
                }
                
                if (u.getIdHorario() < 1) {
                	idHorarioValor = null;
                } else {
                	idHorarioValor = u.getIdHorario();
                }
                
                int tipoTrabajo = 2;
                if(u.getNombrePerfil().equals(perfilCampo)) {
                	tipoTrabajo = 1;
                } 
                
                return new MapSqlParameterSource()
                    // El campo id_equi ahora es opcional y puede ser NULL
                    .addValue("idEquipo", idEquipoValor) 
                    
                    // idHorario sigue siendo obligatorio (se asume que viene válido)
                    .addValue("idHorario", idHorarioValor)
                
                    .addValue("noPersona", (u.getNombres()==null ? "" : u.getNombres().trim().toUpperCase()) + " " + (u.getApePaterno()==null ? "" : u.getApePaterno().trim().toUpperCase()) + " " + (u.getApeMaterno()==null ? "" : u.getApeMaterno().trim().toUpperCase()))
                    
                    .addValue("login", u.getLogin())
                    
                    .addValue("tipoTrabajo", tipoTrabajo)                    
                    
                    // Campos de auditoría
                    .addValue("usuarioSesion", usuarioSesion)
                    .addValue("terminalSesion", terminalSesion)
                    
                    // Campo de filtro (WHERE)
                    .addValue("saaSubj", u.getIdUsuario());
            })
            .toArray(MapSqlParameterSource[]::new);

        // --- 3. Ejecutar la actualización en un solo lote (batchUpdate) ---
        int[] resultados = jdbcTemplate.batchUpdate(SQL_UPDATE, batchParams);
        
        // Opcional: Logging del resultado, aunque el flujo se controla por la excepción
        log.info("Activacion de usuarios existentes completada. Registros activados: " + resultados.length);
    }

    
    @Transactional
    public void registrarUsuariosNuevos(List<UsuarioPerfilDTO> nuevosUsuariosSAA, String perfilCampo, String usuarioSesion, String terminalSesion) {

        if (nuevosUsuariosSAA.isEmpty()) {
        	log.info("Lista de usuarios nuevos está vacía. No se ejecutará la inserción.");
            return;
        }
        
        
        // Preparar la Inserción en Lotes (Batch Update) ---        
        final String SQL_INSERT = "INSERT INTO thaqhirisys.personal (id_pers, no_pers, lg_usua, id_equi, id_hora, saa_sub, ti_trab, st_regi, id_usua_crea, de_term_crea) " +
                                  "VALUES (thaqhirisys.seq_personal.NEXTVAL, :noPersona, :login, :idEquipo, :idHorario, :saaSubj, :tipoTrabajo, 1, :idUsuaCrea, :terminalCrea)";

        // Mapear la lista de usuarios a un array de MapSqlParameterSource (conjuntos de parámetros)
        MapSqlParameterSource[] batchParams = nuevosUsuariosSAA.stream()
            .map(u -> {
                // Lógica para manejar idEquipo: Si es NULL o 0 (si usas 0 como valor inválido), se asigna NULL.
                Object idEquipoValor;
                Object idHorarioValor;
                
                // Asumiendo que idEquipo es de tipo Integer o Long en el DTO
                if (u.getIdEquipo() < 1) {
                    idEquipoValor = null;
                } else {
                    idEquipoValor = u.getIdEquipo();
                }
                
                if (u.getIdHorario() < 1) {
                	idHorarioValor = null;
                } else {
                	idHorarioValor = u.getIdHorario();
                }
                
                int tipoTrabajo = 2;
                if(u.getNombrePerfil().equals(perfilCampo)) {
                	tipoTrabajo = 1;
                }
                
                return new MapSqlParameterSource()
                	.addValue("noPersona", (u.getNombres()==null ? "" : u.getNombres().trim().toUpperCase()) + " " + (u.getApePaterno()==null ? "" : u.getApePaterno().trim().toUpperCase()) + " " + (u.getApeMaterno()==null ? "" : u.getApeMaterno().trim().toUpperCase()))
                    .addValue("login", u.getLogin())
                    
                    // *** CAMBIO APLICADO AQUÍ ***
                    .addValue("idEquipo", idEquipoValor) 
                    
                    .addValue("idHorario", idHorarioValor)
                    .addValue("saaSubj", u.getIdUsuario())
                    .addValue("tipoTrabajo", tipoTrabajo)                    
                    .addValue("idUsuaCrea", usuarioSesion)                
                    .addValue("terminalCrea", terminalSesion);
            })
            .toArray(MapSqlParameterSource[]::new);

        // --- 4. Ejecutar la inserción en un solo lote (batchUpdate) ---
        int[] resultados = jdbcTemplate.batchUpdate(SQL_INSERT, batchParams);
        
        log.info("Registro de usuarios nuevos completada. Registros insertados: " + resultados.length);
    }
    


    /**
     * Inactiva usuarios que no están en la lista de usuarios activos del SAA
     */
    @Transactional
    public void inactivarUsuariosNoVigentes(List<UsuarioPerfilDTO> usuariosActivosSaa, String usuario, String terminal) {
        // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-06 17:12 UTC-5 (Lima)][desc: Evita inactivaciones masivas si SAA devuelve lista vacía o nula][obj: SincronizacionService.inactivarUsuariosNoVigentes guard]
        if (usuariosActivosSaa == null || usuariosActivosSaa.isEmpty()) {
            log.warn("inactivarUsuariosNoVigentes() - Lista SAA vacía/nula. Se omite inactivación para evitar desactivaciones masivas.");
            return;
        }
    	
        // 1. Recuperar IDs de todos los usuarios locales
        List<Long> idsLocalesSaaSub = jdbcTemplate.getJdbcTemplate()
                .query("SELECT saa_sub FROM thaqhirisys.personal where st_regi = 1", (rs, rowNum) -> rs.getLong("saa_sub"));

        String strIdsUsuariosSAA = "";
        Set<Long> idsSaaActivosSet = new HashSet<>();
        
        for (UsuarioPerfilDTO u : usuariosActivosSaa) {
        	idsSaaActivosSet.add(Long.valueOf(u.getIdUsuario()));
        	
        	//Guardo solo para mostrar despues
        	strIdsUsuariosSAA = strIdsUsuariosSAA + "," + u.getIdUsuario();
        }
        
        //log.info("Usuarios Activos en SAA =" + strIdsUsuariosSAA);

        //Guardo solo para mostrar despues
        String strIdsLocalesSaaSub = "";
        for(Long idSaaSubjectLocal : idsLocalesSaaSub) {        	
        	strIdsLocalesSaaSub = strIdsLocalesSaaSub + "," + idSaaSubjectLocal;
            
        }
        
        //log.info("Usuarios Activos en la BD =" + strIdsLocalesSaaSub);
        
        // 2. Determinar IDs que no están en el SAA
        List<Long> idsAInactivar = idsLocalesSaaSub.stream()
                .filter(saaSubject -> !idsSaaActivosSet.contains(saaSubject))
                .toList();

        String strIdsInactivar = "";
            for(Long idInactivar : idsAInactivar) {            	
            	strIdsInactivar = strIdsInactivar + "," + idInactivar;
        }
            
        //log.info("Usuarios a Inactivarse =" + strIdsInactivar);
        
        if(idsAInactivar.size() > 0) {
        	
        	//Guardo solo para mostrar despues
        	
            
        	log.info("inactivarUsuariosNoVigentes() - Total de usuarios a inactivar: " + idsAInactivar.size() + 
        			 ", strIdsUsuariosSAA=" + strIdsUsuariosSAA + 
        			 ", strIdsLocalesSaaSub=" + strIdsLocalesSaaSub + 
        			 ", strIdsInactivar=" + strIdsInactivar);
        }
        else {
        	log.info("inactivarUsuariosNoVigentes() - No existen usuarios por inactivar");
        }
        
        // 3. Ejecutar UPDATE en batches
        int batchSize = 500;
        for (int i = 0; i < idsAInactivar.size(); i += batchSize) {
            List<Long> batch = idsAInactivar.subList(i, Math.min(i + batchSize, idsAInactivar.size()));
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("ids", batch);
            params.addValue("usuario", usuario);
            params.addValue("terminal", terminal);
            
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-02-06 17:12 UTC-5 (Lima)][desc: Corrige asignación SQL (:= -> =) en update de inactivación][obj: SincronizacionService.inactivarUsuariosNoVigentes sql]
            String strSQL = "UPDATE thaqhirisys.personal SET st_regi = 0, id_usua_modi = :usuario, de_term_modi = :terminal WHERE saa_sub IN (:ids)";
            log.info("inactivarUsuariosNoVigentes() - Ejecutando UPDATE para inactivar usuarios, idsAInactivar=" + idsAInactivar.toArray().toString());
            jdbcTemplate.update(strSQL, params);
        }
    }
    
    
    private List<UsuarioPerfilDTO> obtenerUsuariosPorPerfilCampo(List<UsuarioPerfilDTO> usuariosSaa, String perfilCampo) {
    	
        if (usuariosSaa == null || usuariosSaa.isEmpty()) {
            return new ArrayList<>();
        }

        return usuariosSaa.stream()
                .filter(u -> perfilCampo.equals(u.getNombrePerfil()))
                .toList();
    }
    
    private List<UsuarioPerfilDTO> obtenerUsuariosPorPerfilNoCampo(List<UsuarioPerfilDTO> usuariosSaa, String perfilCampo) {
    	
        if (usuariosSaa == null || usuariosSaa.isEmpty()) {
            return new ArrayList<>();
        }

        return usuariosSaa.stream()
                .filter(u -> !perfilCampo.equals(u.getNombrePerfil()))
                .toList();
    }
    

}
