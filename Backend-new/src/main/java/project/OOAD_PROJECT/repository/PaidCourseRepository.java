package project.OOAD_PROJECT.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.OOAD_PROJECT.model.PaidCourse;

import java.util.List;

public interface PaidCourseRepository extends JpaRepository<PaidCourse, Long> {
    List<PaidCourse> findByInstructorId(Long instructorId); // Needed for instructor-specific query
}
