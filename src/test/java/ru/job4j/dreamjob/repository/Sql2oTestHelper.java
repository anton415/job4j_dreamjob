package ru.job4j.dreamjob.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import org.sql2o.quirks.NoQuirks;

final class Sql2oTestHelper {
    private static final String[] SCRIPTS = {
            "db/scripts/001_ddl_create_cities_table.sql",
            "db/scripts/002_dml_insert_cities.sql",
            "db/scripts/003_ddl_create_files_table.sql",
            "db/scripts/004_ddl_create_vacancies_table.sql",
            "db/scripts/004_ddl_create_candidate_table.sql",
            "db/scripts/006_ddl_create_users_table.sql"
    };

    private Sql2oTestHelper() {
    }

    static Sql2o initSql2o() throws SQLException, IOException {
        var dataSource = dataSource();
        dropDatabase(dataSource);
        for (var script : SCRIPTS) {
            runScript(dataSource, script);
        }
        return new Sql2o(dataSource, new NoQuirks(createConverters()));
    }

    private static DataSource dataSource() throws IOException {
        var properties = connectionProperties();
        var dataSource = new JdbcDataSource();
        dataSource.setURL(properties.getProperty("url"));
        dataSource.setUser(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        return dataSource;
    }

    private static Properties connectionProperties() throws IOException {
        var properties = new Properties();
        try (var input = Sql2oTestHelper.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            if (input == null) {
                throw new IllegalStateException("connection.properties not found");
            }
            properties.load(input);
        }
        return properties;
    }

    private static void dropDatabase(DataSource dataSource) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
        }
    }

    private static void runScript(DataSource dataSource, String script) throws SQLException, IOException {
        try (var connection = dataSource.getConnection();
             var reader = Files.newBufferedReader(Path.of(script))) {
            RunScript.execute(connection, reader);
        }
    }

    private static Map<Class, Converter> createConverters() {
        var converters = new HashMap<Class, Converter>();
        converters.put(LocalDateTime.class, localDateTimeConverter());
        return converters;
    }

    private static Converter<LocalDateTime> localDateTimeConverter() {
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
