package project.OOAD_PROJECT.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.OOAD_PROJECT.model.PaidCourse;
import project.OOAD_PROJECT.model.PaidCourseModule;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaidCourseModuleRepository extends JpaRepository<PaidCourseModule, Long> {

    // Get all modules for a course
    List<PaidCourseModule> findByPaidCourse(PaidCourse paidCourse);

    // Get a specific module by its ID and course ID (safe even if ID is globally unique)
    Optional<PaidCourseModule> findByIdAndPaidCourse_Id(Long moduleId, Long courseId);
}
