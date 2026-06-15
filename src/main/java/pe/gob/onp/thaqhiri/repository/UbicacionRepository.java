package pe.gob.onp.thaqhiri.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import pe.gob.onp.thaqhiri.model.UbicacionRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UbicacionRepository {

	private JdbcTemplate jdbc;

    public UbicacionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    

    /**
     * Devuelve los registros de ubicación de las personas especificadas en una fecha
     * o desde un timestamp, manteniendo la misma estructura que antes.
     */
    public Map<String, Object> findByPersonaAndDateWithTimestamp(
            String personaIdsCsv,
            String fechaIso,
            String idUbicacionAnterior) {

        // Convertir CSV en lista limpia
        List<String> idList = Arrays.stream(personaIdsCsv.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());

        if (idList.isEmpty()) {
            return Map.of(
                    "personas", Collections.emptyList(),
                    "timestampConsulta", "0"
            );
        }

        // Generar placeholders para IN clause
        String placeholders = String.join(",", Collections.nCopies(idList.size(), "?"));
        
        // Determinar si se requiere filtrar por el Id de la Ubicacion        
        boolean filtrarPorIdUbicacion = idUbicacionAnterior != null && !idUbicacionAnterior.isEmpty() && !idUbicacionAnterior.equals("0");        
        String idUbicacionCondition = "";
        
        if (filtrarPorIdUbicacion) {
        	idUbicacionCondition = " AND u.ID_UBIC > " + idUbicacionAnterior + " ";
        }

        String sql =
                "SELECT u.id_ubic, "+
                " u.ID_PERS, " +
                " p.NO_PERS AS nombre_persona, " +
                " p.LG_USUA AS usuario_login, " +

                " TO_CHAR(" +
                " (u.fe_ubic::timestamp AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima')," +
                " 'DD/MM/YYYY'" +
                ") as fecha," +

                " TO_CHAR(" +
                " (u.fe_ubic::timestamp AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima')," +
                " 'HH24:MI:SS'" +
                ") as hora," +

                "       u.NU_LONG AS lon, " +
                "       u.NU_LATI AS lat " +
                "FROM UBICACION u " +
                "JOIN PERSONAL p ON u.ID_PERS = p.ID_PERS " +
                "WHERE u.st_regi = 1 " +
                " AND u.FL_FILT = 0 " +
                " AND u.ID_PERS IN (" + placeholders + ") " +
                " AND TO_CHAR( (u.fe_ubic::timestamp AT TIME ZONE 'UTC' AT TIME ZONE 'America/Lima'), 'YYYY-MM-DD' ) = ? " +
                idUbicacionCondition +
                "ORDER BY u.FE_UBIC ASC, u.id_ubic ASC";

        // Parámetros de la query
        List<Object> paramsList = new ArrayList<>(idList);
        paramsList.add(fechaIso);
        
        Object[] params = paramsList.toArray();

        // Ejecutar la consulta y mapear resultados a LocationRecord
        List<UbicacionRecord> registros = jdbc.query(sql, params, (rs, rowNum) -> mapRowToLocationRecord(rs));
        
        //Devuelve el ultimo id de ubicacion de la consulta, como marca para la siguiente consulta
        String ultimoIdUbicacion = idUbicacionAnterior == null ? "0" : idUbicacionAnterior; //Valor predeterminado, si no encuentra datos devuelve el valor de entrada o cero si es null.
        
        if(registros != null && registros.size() > 0) {        	
        	UbicacionRecord ultimaUbicacion = registros.get(registros.size()-1);
        	ultimoIdUbicacion = String.valueOf(ultimaUbicacion.getIdUbicacion());        	
        }
        
        /*
        System.out.println("registros.size(): " + registros.size() +
		           ", idUbicacionAnterior = " + idUbicacionAnterior +  
		           ", filtrarPorIdUbicacion = " + filtrarPorIdUbicacion +
		           ", idUbicacionCondition = " + idUbicacionCondition + 
		           ", ultimoIdUbicacion = " + ultimoIdUbicacion);
        */
        
        return Map.of(
                "personas", registros,
                "timestampConsulta", ultimoIdUbicacion
        );
    }

    private UbicacionRecord mapRowToLocationRecord(ResultSet rs) throws SQLException {
        return new UbicacionRecord(        		
        		rs.getLong("id_ubic"),
                rs.getInt("ID_PERS"),
                rs.getString("nombre_persona"),
                rs.getString("usuario_login"),
                rs.getString("fecha"),
                rs.getString("hora"),
                rs.getDouble("lon"),
                rs.getDouble("lat"),
                0
        );
    }
}
