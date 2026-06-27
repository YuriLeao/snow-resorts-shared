package com.snowresorts.security.jackson;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Applies the project-wide {@code dd/MM/yyyy} date format to JSON payloads and
 * {@code @RequestParam} / {@code @PathVariable} {@code LocalDate} values.
 */
@AutoConfiguration
public class ApiDateTimeAutoConfiguration {

    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    @Bean
    Jackson2ObjectMapperBuilderCustomizer snowLocalDateJacksonCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateSerializer(DATE_FORMATTER));
            builder.deserializers(new LocalDateDeserializer(DATE_FORMATTER));
        };
    }

    @Bean
    WebMvcConfigurer snowLocalDateWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addFormatters(FormatterRegistry registry) {
                DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
                registrar.setDateFormatter(DATE_FORMATTER);
                registrar.registerFormatters(registry);
            }
        };
    }
}
