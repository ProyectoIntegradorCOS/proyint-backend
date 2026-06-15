package pe.gob.onp.thaqhiri.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import pe.gob.onp.thaqhiri.exception.BusinessException;

@Service
public class MapboxGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(MapboxGeocodingService.class);

    private final WebClient webClient;
    private final String accessToken;
    private final String geocodeApi;
    private final MeterRegistry meterRegistry;

    public MapboxGeocodingService(WebClient webClient,
                                 @Value("${mapbox.access-token:}") String accessToken,
                                 @Value("${mapbox.geocode-api:v6}") String geocodeApi,
                                 MeterRegistry meterRegistry) {
        this.webClient = webClient;
        this.accessToken = accessToken != null ? accessToken.trim() : "";
        this.geocodeApi = geocodeApi != null ? geocodeApi.trim().toLowerCase() : "v6";
        this.meterRegistry = meterRegistry;
    }

    
    private String truncarTokens(String cadena, int numeroTokens) {

        if (cadena == null || cadena.isBlank()) {
            return cadena;
        }

        // 1️⃣ Eliminar texto entre paréntesis
        cadena = cadena.replaceAll("\\(.*?\\)", " ");

        // 2️⃣ Reemplazar guiones y comas por espacio
        cadena = cadena.replaceAll("[-,]", " ");

        // 3️⃣ Normalizar múltiples espacios
        cadena = cadena.replaceAll("\\s+", " ").trim();

        // 4️⃣ Separar tokens
        String[] tokens = cadena.split("\\s+");

        if (tokens.length <= numeroTokens) {
            return cadena;
        }

        // 5️⃣ Tomar solo los primeros 20
        return String.join(" ", Arrays.copyOfRange(tokens, 0, numeroTokens));
    }
    
    private int obtenerNumeroTokens(String cadena) {

    	if (cadena == null || cadena.isBlank()) {
            return -1;
        }

        // 1️⃣ Eliminar texto entre paréntesis
        cadena = cadena.replaceAll("\\(.*?\\)", " ");

        // 2️⃣ Reemplazar guiones y comas por espacio
        cadena = cadena.replaceAll("[-,]", " ");

        // 3️⃣ Normalizar múltiples espacios
        cadena = cadena.replaceAll("\\s+", " ").trim();

        // 4️⃣ Separar tokens
        String[] tokens = cadena.split("\\s+");

        return tokens.length;
    }
    
    
    private String normalizeDireccion(String direccion) {
        return direccion
                .replace("NRO. ", "")
                .replace("URB.", "")
                .replace("AV. ", "AV ")
                .replace("  ", " ")
                .trim()
                .toUpperCase();
    }
    
    
    public Optional<Suggestion> forwardGeocode(String direccionOriginal) {
    	
        if (accessToken.isEmpty()) {
            recordGeocodeMetric("forward", "disabled");
            return Optional.empty();
        }
        
        if (direccionOriginal == null || direccionOriginal.trim().isEmpty()) {
            recordGeocodeMetric("forward", "invalid");
            return Optional.empty();
        }

        String direccionNormalizada= normalizeDireccion(direccionOriginal);
        
        //Trunca a 20 tokens, debido a que es lo maximo que soporta mapbox
        int numeroTokensActual = obtenerNumeroTokens(direccionNormalizada);
        String query = truncarTokens(direccionNormalizada, 20);
        int numeroTokensNuevo = obtenerNumeroTokens(query);
        
        if(numeroTokensNuevo < numeroTokensActual) {
        	log.warn("forwardGeocode(), se redujeron los tokens (de " + numeroTokensActual + " a " + numeroTokensNuevo + ") de la dirección, direccionNormalizada inicial=" + direccionNormalizada + ", final=" + query);
        }
        
        if ("v5".equals(geocodeApi)) {
            Optional<Suggestion> v5 = forwardV5(query.trim());
            recordGeocodeMetric("forward", v5.isPresent() ? "success" : "empty");
            return v5;
        }

        // Preferir v6 y hacer fallback a v5 si no hay resultados (útil si el token/plan restringe v6).
        try {
            Optional<Suggestion> v6 = forwardV6(query.trim());
            if (v6.isPresent()) {
                recordGeocodeMetric("forward", "success");
                return v6;
            }
            Optional<Suggestion> v5 = forwardV5(query.trim());
            recordGeocodeMetric("forward", v5.isPresent() ? "success" : "empty");
            return v5;
        } catch (MapboxException ex) {
            log.warn("Mapbox forward(v6) falló ({}). Se intenta v5.", ex.getMessage());
            recordGeocodeMetric("forward", "error");
            
            Optional<Suggestion> v5 = null;
            
            try {
            	//Reducir los tokens
            	query = truncarTokens(direccionNormalizada, 18);
            	v5 = forwardV5(query.trim());
            	
            } catch (MapboxException e) {
            	log.warn("Mapbox forward(v5) falló ({}). ", ex.getMessage());
            	throw new BusinessException("No se pudo convertir la direccion '" + direccionOriginal + "' a coordenadas. Revise que no supere los 20 tokens.");
            }            
            
            recordGeocodeMetric("forward", v5.isPresent() ? "success" : "empty");
            return v5;
        }
    }

    public Optional<ReverseSuggestion> reverseGeocode(double lat, double lng) {
        if (accessToken.isEmpty()) {
            recordGeocodeMetric("reverse", "disabled");
            return Optional.empty();
        }

        if ("v5".equals(geocodeApi)) {
            Optional<ReverseSuggestion> v5 = reverseV5(lat, lng);
            recordGeocodeMetric("reverse", v5.isPresent() ? "success" : "empty");
            return v5;
        }

        try {
            Optional<ReverseSuggestion> v6 = reverseV6(lat, lng);
            if (v6.isPresent()) {
                recordGeocodeMetric("reverse", "success");
                return v6;
            }
            Optional<ReverseSuggestion> v5 = reverseV5(lat, lng);
            recordGeocodeMetric("reverse", v5.isPresent() ? "success" : "empty");
            return v5;
        } catch (MapboxException ex) {
            log.warn("Mapbox reverse(v6) falló ({}). Se intenta v5.", ex.getMessage());
            recordGeocodeMetric("reverse", "error");
            Optional<ReverseSuggestion> v5 = reverseV5(lat, lng);
            recordGeocodeMetric("reverse", v5.isPresent() ? "success" : "empty");
            return v5;
        }
    }

    public boolean isConfigured() {
        return !accessToken.isEmpty();
    }

    private Map<String, Object> fetch(
            Function<UriBuilder, URI> uriFn
    ) {

        long start = System.nanoTime();
        String status = "success";
        try {
            Map<String, Object> resp = webClient.get()
                    .uri(uriFn)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(java.time.Duration.ofSeconds(15))
                    .block();

            return resp != null ? resp : Map.of();

        } catch (WebClientResponseException ex) {

            log.error("Mapbox error {} body={}",
                    ex.getStatusCode().value(),
                    ex.getResponseBodyAsString());

            status = "error";
            recordGeocodeMetric("fetch", "error");
            throw new MapboxException(
                    "Mapbox respondió " + ex.getStatusCode().value(),
                    ex
            );

        } catch (Exception ex) {
            status = "error";
            recordGeocodeMetric("fetch", "error");
            throw new MapboxException("No se pudo consultar Mapbox", ex);
        } finally {
            // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:27 UTC-5 (Lima)][desc: Registra latencia de geocodificación][obj: MapboxGeocodingService.fetch]
            meterRegistry.timer("thaqhiri_backend_geocode_duration", "status", status)
                    .record(System.nanoTime() - start, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
    }

    // [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-23 12:01 UTC-5 (Lima)][desc: Registra métricas de geocodificación para incidentes][obj: MapboxGeocodingService.recordGeocodeMetric]
    private void recordGeocodeMetric(String type, String status) {
        meterRegistry.counter(
                "thaqhiri_backend_geocode_total",
                Tags.of("type", type, "status", status)
        ).increment();
    }

    

    private Optional<ParsedFeature> parseFirstFeatureV5(Map<String, Object> resp) {
        if (resp == null) return Optional.empty();
        Object featuresObj = resp.get("features");
        if (!(featuresObj instanceof java.util.List<?> features) || features.isEmpty()) {
            return Optional.empty();
        }
        Object firstObj = features.get(0);
        if (!(firstObj instanceof Map<?, ?> first)) {
            return Optional.empty();
        }

        Double lat = null;
        Double lng = null;
        Object centerObj = first.get("center");
        if (centerObj instanceof java.util.List<?> center && center.size() >= 2) {
            Object x = center.get(0);
            Object y = center.get(1);
            if (x instanceof Number nx && y instanceof Number ny) {
                lng = nx.doubleValue();
                lat = ny.doubleValue();
            }
        }

        if (lat == null || lng == null) {
            return Optional.empty();
        }

        String placeId = readString(first, "id");
        String label = readString(first, "place_name");

        String depa = null;
        String prov = null;
        String dist = null;
        Object ctxObj = first.get("context");
        if (ctxObj instanceof java.util.List<?> ctxList) {
            for (Object ctxItem : ctxList) {
                if (!(ctxItem instanceof Map<?, ?> ctx)) continue;
                String id = readString(ctx, "id");
                String text = readString(ctx, "text");
                if (id == null || text == null) continue;
                if (id.startsWith("region.")) depa = text;
                if (id.startsWith("place.")) prov = text;
                if (id.startsWith("district.")) dist = text;
                if (dist == null && id.startsWith("locality.")) dist = text;
            }
        }

        return Optional.of(new ParsedFeature(placeId, label, lat, lng, depa, prov, dist));
    }

    @SuppressWarnings("unchecked")
    private Optional<ParsedFeature> parseFirstFeatureV6(Map<String, Object> resp) {
        if (resp == null) return Optional.empty();
        Object featuresObj = resp.get("features");
        if (!(featuresObj instanceof java.util.List<?> features) || features.isEmpty()) {
            return Optional.empty();
        }
        Object firstObj = features.get(0);
        if (!(firstObj instanceof Map<?, ?> first)) {
            return Optional.empty();
        }

        Double lat = null;
        Double lng = null;
        Object geometryObj = first.get("geometry");
        if (geometryObj instanceof Map<?, ?> geom) {
            Object coordsObj = geom.get("coordinates");
            if (coordsObj instanceof java.util.List<?> coords && coords.size() >= 2) {
                Object x = coords.get(0);
                Object y = coords.get(1);
                if (x instanceof Number nx && y instanceof Number ny) {
                    lng = nx.doubleValue();
                    lat = ny.doubleValue();
                }
            }
        }
        if (lat == null || lng == null) return Optional.empty();

        String placeId = readString(first, "id");
        String label = null;

        Object propsObj = first.get("properties");
        if (propsObj instanceof Map<?, ?> props) {
            label = readString(props, "full_address");
            if (label == null) label = readString(props, "place_formatted");
            if (label == null) label = readString(props, "name");
            if (placeId == null) placeId = readString(props, "mapbox_id");

            String depa = null, prov = null, dist = null;
            Object ctxObj = props.get("context");
            if (ctxObj instanceof Map<?, ?> ctx) {
                depa = readNestedName(ctx, "region");
                prov = readNestedName(ctx, "place");
                dist = readNestedName(ctx, "district");
                if (dist == null) dist = readNestedName(ctx, "locality");
            }

            if (label == null) label = readString(first, "place_name");
            return Optional.of(new ParsedFeature(placeId, label, lat, lng, depa, prov, dist));
        }

        label = readString(first, "place_name");
        return Optional.of(new ParsedFeature(placeId, label, lat, lng, null, null, null));
    }

    
    private Optional<Suggestion> forwardV6(String query) {

        String cleanQuery = normalizeQuery(query);

      //-------------------------------------------
        String[] tokens = cleanQuery.split("\\s+");
        if (tokens.length > 20) {
        	log.info("Direccion mayor a 20 tokens: " + cleanQuery);
        };
        //-------------------------------------------
        
        
        //log.info("forwardV6(), query=" + query);
        
        Map<String, Object> resp = fetch(uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host("api.mapbox.com")
                    .path("/search/geocode/v6/forward")
                    .queryParam("q", cleanQuery)
                    .queryParam("limit", 1)
                    .queryParam("language", "es")
                    .queryParam("bbox", "-81.35,-18.35,-68.65,-0.03") // ← PERÚ
                    .queryParam("access_token", accessToken)
                    .build()
        );

        var parsed = parseFirstFeatureV6(resp);
        if (parsed.isEmpty()) return Optional.empty();

        var p = parsed.get();
        //log.info("Mapbox forward(v6): '{}' -> {}", cleanQuery, p.label);

        return Optional.of(new Suggestion(
                p.placeId,
                p.label,
                p.lat,
                p.lng,
                OffsetDateTime.now()
        ));
    }

    private Optional<Suggestion> forwardV5(String query) {

        String cleanQuery = normalizeQuery(query);

        Map<String, Object> resp = fetch(uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host("api.mapbox.com")
                    .path("/geocoding/v5/mapbox.places/{query}.json")
                    .queryParam("access_token", accessToken)
                    .queryParam("country", "pe")
                    .queryParam("language", "es")
                    .queryParam("limit", 1)
                    .build(cleanQuery)
        );

        var parsed = parseFirstFeatureV5(resp);
        if (parsed.isEmpty()) return Optional.empty();

        var p = parsed.get();
        log.info("Mapbox forward(v5): '{}' -> {}", cleanQuery, p.label);

        return Optional.of(new Suggestion(
                p.placeId,
                p.label,
                p.lat,
                p.lng,
                OffsetDateTime.now()
        ));
    }



    private Optional<ReverseSuggestion> reverseV6(double lat, double lng) {

        Map<String, Object> resp = fetch(uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host("api.mapbox.com")
                    .path("/search/geocode/v6/reverse")
                    .queryParam("longitude", lng)
                    .queryParam("latitude", lat)
                    .queryParam("limit", 1)
                    .queryParam("language", "es")
                    .queryParam("access_token", accessToken)
                    .build()
        );

        var parsed = parseFirstFeatureV6(resp);
        if (parsed.isEmpty()) return Optional.empty();

        var p = parsed.get();
        log.info("Mapbox reverse(v6): {},{} -> {}", lat, lng, p.label);

        return Optional.of(new ReverseSuggestion(
                p.placeId,
                p.label,
                p.label,
                p.departamento,
                p.provincia,
                p.distrito,
                OffsetDateTime.now()
        ));
    }


    private Optional<ReverseSuggestion> reverseV5(double lat, double lng) {

        Map<String, Object> resp = fetch(uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host("api.mapbox.com")
                    .path("/geocoding/v5/mapbox.places/{coords}.json")
                    .queryParam("access_token", accessToken)
                    .queryParam("country", "pe")
                    .queryParam("language", "es")
                    .queryParam("limit", 1)
                    .build(lng + "," + lat)
        );

        var parsed = parseFirstFeatureV5(resp);
        if (parsed.isEmpty()) return Optional.empty();

        var p = parsed.get();
        log.info("Mapbox reverse(v5): {},{} -> {}", lat, lng, p.label);

        return Optional.of(new ReverseSuggestion(
                p.placeId,
                p.label,
                p.label,
                p.departamento,
                p.provincia,
                p.distrito,
                OffsetDateTime.now()
        ));
    }

    

    private String encodePathSegment(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String readString(Map<?, ?> map, String key) {
        Object v = map.get(key);
        if (v instanceof String s && !s.isBlank()) return s;
        return null;
    }

    private String readNestedName(Map<?, ?> ctx, String key) {
        Object o = ctx.get(key);
        if (!(o instanceof Map<?, ?> m)) return null;
        return readString(m, "name");
    }

    public record Suggestion(String placeId, String label, double lat, double lng, OffsetDateTime at) {}

    public record ReverseSuggestion(
            String placeId,
            String label,
            String direccion,
            String departamento,
            String provincia,
            String distrito,
            OffsetDateTime at
    ) {}

    private record ParsedFeature(
            String placeId,
            String label,
            double lat,
            double lng,
            String departamento,
            String provincia,
            String distrito
    ) {}

    public static class MapboxException extends RuntimeException {
        public MapboxException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    
    private String normalizeQuery(String q) {
        return q == null
                ? ""
                : q.trim()
                     .replaceAll("[\\n\\r\\t]", " ")
                     .replaceAll("\\s+", " ");
    }
    
}
