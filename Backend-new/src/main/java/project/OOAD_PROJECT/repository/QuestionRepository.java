package project.OOAD_PROJECT.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.OOAD_PROJECT.model.Question;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAssessmentId(Long assessmentId);
    void deleteByAssessmentId(Long assessmentId);
} 