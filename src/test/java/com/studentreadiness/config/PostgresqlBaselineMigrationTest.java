package com.studentreadiness.config;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresqlBaselineMigrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "ASSESSMENT_ATTEMPTS",
            "ASSESSMENT_OPTIONS",
            "ASSESSMENT_QUESTIONS",
            "ASSESSMENT_RESPONSES",
            "ASSESSMENTS",
            "CERTIFICATIONS",
            "JOB_ROLE_SKILLS",
            "JOB_ROLES",
            "LEARNING_RECOMMENDATIONS",
            "LEARNING_RESOURCES",
            "SKILLS",
            "STUDENT_PROFILES",
            "STUDENT_PROJECTS",
            "STUDENT_SKILLS",
            "USERS"
    );

    @Test
    void postgresBaselineCreatesAllMappedTablesInH2PostgresqlMode() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:postgres-baseline-" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/postgresql")
                .load()
                .migrate();

        Set<String> actualTables = new HashSet<>();
        try (var connection = dataSource.getConnection();
             ResultSet tables = connection.getMetaData()
                     .getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (!"flyway_schema_history".equalsIgnoreCase(tableName)) {
                    actualTables.add(tableName.toUpperCase());
                }
            }
        }

        assertEquals(EXPECTED_TABLES, actualTables);
    }
}
