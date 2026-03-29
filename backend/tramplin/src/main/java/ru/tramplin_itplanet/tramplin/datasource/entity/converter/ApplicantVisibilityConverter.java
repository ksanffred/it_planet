package ru.tramplin_itplanet.tramplin.datasource.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;

@Converter(autoApply = false)
public class ApplicantVisibilityConverter implements AttributeConverter<ApplicantVisibility, String> {

    @Override
    public String convertToDatabaseColumn(ApplicantVisibility attribute) {
        ApplicantVisibility value = attribute != null ? attribute : ApplicantVisibility.PRIVATE;
        return value.name();
    }

    @Override
    public ApplicantVisibility convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return ApplicantVisibility.PRIVATE;
        }
        return ApplicantVisibility.valueOf(dbData.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
