package br.com.frotasPro.api.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.math.BigDecimal;

public class FlexibleBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDecimalValue();
        }

        if (token == JsonToken.VALUE_STRING) {
            String raw = p.getText();
            if (raw == null) {
                return null;
            }

            String valor = raw.trim();
            if (valor.isEmpty()) {
                return null;
            }

            try {
                if (valor.contains(",")) {
                    // Ex.: 8.905,46 -> 8905.46
                    valor = valor.replace(".", "").replace(",", ".");
                }
                return new BigDecimal(valor);
            } catch (NumberFormatException ex) {
                throw InvalidFormatException.from(p, "Valor decimal inválido: " + raw, raw, BigDecimal.class);
            }
        }

        return (BigDecimal) ctxt.handleUnexpectedToken(BigDecimal.class, p);
    }
}
