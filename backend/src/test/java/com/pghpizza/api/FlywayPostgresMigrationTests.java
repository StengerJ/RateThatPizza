package com.pghpizza.api;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.jdbc.DataSourceBuilder;

@Testcontainers(disabledWithoutDocker = true)
class FlywayPostgresMigrationTests {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pgh_pizza")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    void migrationsCreateExpectedTablesInPostgres() {
        DataSource dataSource = DataSourceBuilder.create()
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .build();

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isPositive();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("6");
    }
}
