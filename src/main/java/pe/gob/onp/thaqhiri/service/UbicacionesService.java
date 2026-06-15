package pe.gob.onp.thaqhiri.service;

import pe.gob.onp.thaqhiri.model.UbicacionRecord;
import pe.gob.onp.thaqhiri.repository.UbicacionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UbicacionesService {

    private final ObjectMapper om = new ObjectMapper();
    private final UbicacionRepository repo;
    
    public UbicacionesService(UbicacionRepository repo) {
        this.repo = repo;
    }

    
    /**
     * Nuevo método: Devuelve también la marca de tiempo de la consulta.
     * Esto permitirá al frontend solicitar solo los nuevos puntos en llamadas posteriores.
     */
    public String getGeoJsonByPersonaAndDateWithTimestamp(String personaIdsCsv, String fechaIso, String timestamp) throws Exception {
    	
        // Obtenemos tanto los registros como el timestamp de la consulta
        Map<String, Object> resultado = repo.findByPersonaAndDateWithTimestamp(personaIdsCsv, fechaIso, timestamp);

        @SuppressWarnings("unchecked")
        List<UbicacionRecord> rows = (List<UbicacionRecord>) resultado.get("personas");
        String timestampConsulta = (String) resultado.get("timestampConsulta");
        
        return buildGeoJson(rows, timestampConsulta);
    }
    
    
    /**
     * Construye el JSON GeoJSON a partir de la lista de LocationRecord y un timestamp opcional.
     */
    private String buildGeoJson(List<UbicacionRecord> rows, String timestampConsulta) throws Exception {
        Map<Integer, List<UbicacionRecord>> porUsuario = rows.stream()
                .collect(Collectors.groupingBy(UbicacionRecord::getIdPersona));

        ArrayNode personasArray = om.createArrayNode();

        for (Map.Entry<Integer, List<UbicacionRecord>> entry : porUsuario.entrySet()) {
            Integer idPersona = entry.getKey();
            List<UbicacionRecord> lista = entry.getValue();

            ObjectNode personaNode = om.createObjectNode();
            personaNode.put("user_id", idPersona);
            personaNode.put("usuario", lista.get(0).getUsuario());

            ObjectNode featureCollection = om.createObjectNode();
            featureCollection.put("type", "FeatureCollection");
            ArrayNode features = om.createArrayNode();

            for (UbicacionRecord r : lista) {
                ObjectNode feature = om.createObjectNode();
                feature.put("type", "Feature");

                // geometry
                ObjectNode geometry = om.createObjectNode();
                geometry.put("type", "Point");
                ArrayNode coords = om.createArrayNode();
                coords.add(r.getLon());
                coords.add(r.getLat());
                geometry.set("coordinates", coords);
                feature.set("geometry", geometry);

                // properties
                ObjectNode props = om.createObjectNode();
                props.put("correlativo", r.getCorrelativo());
                props.put("user_id", r.getIdPersona());
                props.put("nombre", r.getNombrePersona());
                props.put("usuario", r.getUsuario());
                props.put("fecha", r.getFecha());
                props.put("hora", r.getHora());
                feature.set("properties", props);

                features.add(feature);
            }

            featureCollection.set("features", features);
            personaNode.set("data", featureCollection);
            personasArray.add(personaNode);
        }

        ObjectNode root = om.createObjectNode();
        root.put("type", "MultiPersonGeoJSON");
        root.set("personas", personasArray);

        if (timestampConsulta != null) {
            root.put("timestampConsulta", timestampConsulta);
        }

        return om.writeValueAsString(root);
    }
    

}
