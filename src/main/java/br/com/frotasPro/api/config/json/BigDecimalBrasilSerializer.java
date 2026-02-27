package br.com.frotasPro.api.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class BigDecimalBrasilSerializer extends JsonSerializer<BigDecimal> {

    private static final DecimalFormat FORMATTER;

    static {
        Locale br = new Locale("pt", "BR");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(br);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        FORMATTER = new DecimalFormat("#,##0.##", symbols);
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        BigDecimal ajustado = value.stripTrailingZeros();
        String formatado = FORMATTER.format(ajustado);

        gen.writeString(formatado);
    }

    @Override
    public void serializeWithType(BigDecimal value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        WritableTypeId typeId = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_STRING));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeId);
    }
}
