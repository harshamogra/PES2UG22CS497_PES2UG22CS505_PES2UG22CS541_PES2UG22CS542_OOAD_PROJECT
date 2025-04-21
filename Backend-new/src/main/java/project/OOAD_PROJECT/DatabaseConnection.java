package project.OOAD_PROJECT;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseConnection {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnection(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void testDatabaseConnection() {
        try {
            // ⚠️ Drop the student table if it exists
            String dropTableQuery = "DROP TABLE IF EXISTS student;";
            jdbcTemplate.execute(dropTableQuery);
            System.out.println("🗑️ Table 'student' dropped (if existed).");

            // ✅ Create the student table
            String createTableQuery = "CREATE TABLE student (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "standard VARCHAR(50)" +
                    ");";
            jdbcTemplate.execute(createTableQuery);
            System.out.println("✅ Table 'student' created.");

            // ✅ Insert dummy data
            String insertDataQuery1 = "INSERT INTO student (name, standard) VALUES ('John Doe', '10th');";
            String insertDataQuery2 = "INSERT INTO student (name, standard) VALUES ('Jane Smith', '12th');";
            String insertDataQuery3 = "INSERT INTO student (name, standard) VALUES ('Alice Johnson', '11th');";
            jdbcTemplate.update(insertDataQuery1);
            jdbcTemplate.update(insertDataQuery2);
            jdbcTemplate.update(insertDataQuery3);
            System.out.println("✅ Dummy data inserted into 'student' table.");

            // ✅ Select and print student records
            String selectQuery = "SELECT * FROM student;";
            jdbcTemplate.query(selectQuery, (rs, rowNum) -> {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Standard: " + rs.getString("standard"));
                return null;
            });

        } catch (Exception e) {
            System.err.println("❌ Database operation failed: " + e.getMessage());
        }
    }
}
