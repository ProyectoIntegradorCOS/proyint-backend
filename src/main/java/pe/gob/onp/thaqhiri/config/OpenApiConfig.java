package pe.gob.onp.thaqhiri.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ONP Thaqhiri API",
                version = "1.0.0",
                description = "API para registro de ubicaciones, historial y usuarios de la aplicación ONP Thaqhiri.",
                contact = @Contact(
                        name = "Equipo ONP Thaqhiri",
                        email = "soporte@onp.gob.pe",
                        url = "https://www.onp.gob.pe"
                ),
                license = @License(
                        name = "UNLICENSED",
                        url = "https://www.onp.gob.pe"
                )
        ),
        servers = {
                @Server(url = "/", description = "Servidor actual")
        }
)
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer apiDocCustomiser() {
        return openApi -> {
            ensureInfoCompleteness(openApi);
            ensureOperationDescriptions(openApi);
            ensureParameterDescriptions(openApi);
            ensurePathParametersAtPathLevel(openApi);
            ensureTagsDefinedAndSorted(openApi);
        };
    }

    private void ensureInfoCompleteness(OpenAPI openApi) {
        if (openApi.getInfo() == null) {
            openApi.setInfo(new io.swagger.v3.oas.models.info.Info());
        }
        var info = openApi.getInfo();
        if (info.getContact() == null) {
            info.setContact(new io.swagger.v3.oas.models.info.Contact());
        }
        var contact = info.getContact();
        if (isBlank(contact.getEmail())) {
            contact.setEmail("soporte@onp.gob.pe");
        }
        if (isBlank(contact.getUrl())) {
            contact.setUrl("https://www.onp.gob.pe");
        }
        if (info.getLicense() == null) {
            info.setLicense(new io.swagger.v3.oas.models.info.License());
        }
        var license = info.getLicense();
        if (isBlank(license.getName())) {
            license.setName("UNLICENSED");
        }
        if (isBlank(license.getUrl())) {
            license.setUrl("https://www.onp.gob.pe");
        }
    }

    private void ensureOperationDescriptions(OpenAPI openApi) {
        Paths paths = openApi.getPaths();
        if (paths == null) return;
        for (PathItem item : paths.values()) {
            if (item == null) continue;
            for (Operation op : item.readOperations()) {
                if (op == null) continue;
                if (isBlank(op.getDescription())) {
                    String fallback = firstNonBlank(op.getSummary(), op.getOperationId(), "Operación");
                    op.setDescription(fallback);
                }
            }
        }
    }

    private void ensureParameterDescriptions(OpenAPI openApi) {
        Paths paths = openApi.getPaths();
        if (paths == null) return;
        for (PathItem item : paths.values()) {
            if (item == null) continue;
            for (Operation op : item.readOperations()) {
                if (op == null || op.getParameters() == null) continue;
                for (Parameter p : op.getParameters()) {
                    if (p == null) continue;
                    if (isBlank(p.getDescription())) {
                        p.setDescription(defaultParamDescription(p));
                    }
                }
            }
        }
    }

    private void ensurePathParametersAtPathLevel(OpenAPI openApi) {
        Paths paths = openApi.getPaths();
        if (paths == null) return;
        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            PathItem item = entry.getValue();
            if (item == null) continue;
            List<Parameter> pathLevelParams = item.getParameters();
            if (pathLevelParams == null) {
                pathLevelParams = new ArrayList<>();
                item.setParameters(pathLevelParams);
            }
            Map<String, Parameter> existing = new HashMap<>();
            for (Parameter p : pathLevelParams) {
                if (p != null && "path".equalsIgnoreCase(p.getIn()) && p.getName() != null) {
                    existing.put(p.getName(), p);
                }
            }
            for (Operation op : item.readOperations()) {
                if (op == null || op.getParameters() == null) continue;
                for (Parameter p : op.getParameters()) {
                    if (p == null || !"path".equalsIgnoreCase(p.getIn()) || p.getName() == null) continue;
                    if (!existing.containsKey(p.getName())) {
                        Parameter clone = new Parameter();
                        clone.setIn("path");
                        clone.setName(p.getName());
                        clone.setRequired(true);
                        clone.setSchema(p.getSchema());
                        clone.setDescription(isBlank(p.getDescription())
                                ? defaultParamDescription(p)
                                : p.getDescription());
                        pathLevelParams.add(clone);
                        existing.put(p.getName(), clone);
                    }
                }
            }
        }
    }

    private void ensureTagsDefinedAndSorted(OpenAPI openApi) {
        Paths paths = openApi.getPaths();
        Set<String> opTags = new HashSet<>();
        if (paths != null) {
            for (PathItem item : paths.values()) {
                if (item == null) continue;
                for (Operation op : item.readOperations()) {
                    if (op == null || op.getTags() == null) continue;
                    opTags.addAll(op.getTags());
                }
            }
        }

        List<Tag> tags = openApi.getTags();
        if (tags == null) {
            tags = new ArrayList<>();
            openApi.setTags(tags);
        }
        Set<String> existing = new HashSet<>();
        for (Tag t : tags) {
            if (t != null && t.getName() != null) {
                existing.add(t.getName());
            }
        }
        for (String t : opTags) {
            if (!existing.contains(t)) {
                tags.add(new Tag().name(t));
            }
        }
        tags.sort(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER));
    }

    private String defaultParamDescription(Parameter p) {
        String name = p.getName() == null ? "parámetro" : p.getName();
        String in = p.getIn() == null ? "" : p.getIn();
        switch (in) {
            case "path":
                return "Parámetro de ruta: " + name;
            case "query":
                return "Parámetro de consulta: " + name;
            case "header":
                return "Encabezado: " + name;
            case "cookie":
                return "Cookie: " + name;
            default:
                return "Parámetro: " + name;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }
}
