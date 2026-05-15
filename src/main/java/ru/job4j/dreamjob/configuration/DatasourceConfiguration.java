package ru.job4j.dreamjob.configuration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import org.sql2o.quirks.NoQuirks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasourceConfiguration {

    @Bean(destroyMethod = "close")
    public BasicDataSource connectionPool(@Value("${datasource.url}") String url,
                                          @Value("${datasource.username}") String username,
                                          @Value("${datasource.password}") String password) {
        var dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    public Sql2o databaseClient(BasicDataSource connectionPool) {
        return new Sql2o(connectionPool, new NoQuirks(createConverters()));
    }

    private Map<Class, Converter> createConverters() {
        var converters = new HashMap<Class, Converter>();
        converters.put(LocalDateTime.class, localDateTimeConverter());
        return converters;
    }

    private Converter<LocalDateTime> localDateTimeConverter() {
        return new Converter<>() {
            @Override
            public LocalDateTime convert(Object value) throws ConverterException {
                if (value == null) {
                    return null;
                }
                if (value instanceof Timestamp timestamp) {
                    return timestamp.toLocalDateTime();
                }
                if (value instanceof LocalDateTime localDateTime) {
                    return localDateTime;
                }
                throw new ConverterException("Cannot convert value to LocalDateTime");
            }

            @Override
            public Object toDatabaseParam(LocalDateTime value) {
                return value == null ? null : Timestamp.valueOf(value);
            }
        };
    }
}
