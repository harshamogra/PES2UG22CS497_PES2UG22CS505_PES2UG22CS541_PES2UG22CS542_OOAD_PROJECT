package project.OOAD_PROJECT.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.OOAD_PROJECT.model.Assessment;
import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    Optional<Assessment> findByCourseIdAndModuleId(Long courseId, Long moduleId);
    List<Assessment> findByCourseId(Long courseId);
} 