package project.OOAD_PROJECT.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import project.OOAD_PROJECT.model.FreeCourse;



import java.util.List;

@Repository
public class FreeCourseRepository {

    private final JdbcTemplate jdbcTemplate;

    public FreeCourseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FreeCourse> getAllCourses() {
        return jdbcTemplate.query("SELECT * FROM free_courses", (rs, rowNum) -> {
            FreeCourse course = new FreeCourse();
            course.setId(rs.getLong("id"));
            course.setCourseName(rs.getString("course_name"));
            course.setDescription(rs.getString("description"));
            course.setLink(rs.getString("link"));
            return course;
        });
    }

    public void saveCourse(FreeCourse course) {
        String sql = "INSERT INTO free_courses (course_name, description, link) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, course.getCourseName(), course.getDescription(), course.getLink());
    }
    
    public void deleteCourseById(Long id) {
        String sql = "DELETE FROM free_courses WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
}
