package com.snowresorts.security.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class ApiDateTimeAutoConfigurationTest {

    private final ObjectMapper objectMapper = buildObjectMapper();

    private static ObjectMapper buildObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new ApiDateTimeAutoConfiguration().snowLocalDateJacksonCustomizer().customize(builder);
        return builder.build();
    }

    @Test
    @DisplayName("LocalDate serializes as dd/MM/yyyy in JSON")
    void serialize_localDate_usesBrazilianFormat() throws Exception {
        String json = objectMapper.writeValueAsString(new DatePayload(LocalDate.of(2026, 6, 23)));

        assertThat(json).contains("\"visitedAt\":\"23/06/2026\"");
    }

    @Test
    @DisplayName("LocalDate deserializes from dd/MM/yyyy JSON")
    void deserialize_localDate_parsesBrazilianFormat() throws Exception {
        DatePayload payload = objectMapper.readValue("{\"visitedAt\":\"23/06/2026\"}", DatePayload.class);

        assertThat(payload.visitedAt()).isEqualTo(LocalDate.of(2026, 6, 23));
    }

    private record DatePayload(LocalDate visitedAt) {
    }
}
