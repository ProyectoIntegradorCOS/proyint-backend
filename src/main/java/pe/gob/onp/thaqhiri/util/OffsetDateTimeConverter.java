package pe.gob.onp.thaqhiri.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

// El autoApply = true hace que JPA use este conversor automáticamente en todos los OffsetDateTime,
// pero lo aplicaremos manualmente para estar seguros.
@Converter(autoApply = true)
public class OffsetDateTimeConverter implements AttributeConverter<OffsetDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(OffsetDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        // PASO CLAVE: Normalizamos el momento A UTC ANTES de convertir a Timestamp.
        // Esto elimina cualquier ambigüedad de zona horaria del sistema o JDBC.
        OffsetDateTime utcTime = attribute.withOffsetSameInstant(ZoneOffset.UTC);
        return Timestamp.from(utcTime.toInstant());
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(Timestamp dbData) {
        if (dbData == null) {
            return null;
        }
        // Al leer, asumimos que el Timestamp almacenado representa un momento UTC,
        // y lo etiquetamos explícitamente como UTC.
        return OffsetDateTime.ofInstant(dbData.toInstant(), ZoneOffset.UTC);
    }
}