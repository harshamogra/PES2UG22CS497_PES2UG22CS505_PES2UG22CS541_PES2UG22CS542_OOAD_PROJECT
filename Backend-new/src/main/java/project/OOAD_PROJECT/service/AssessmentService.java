package project.OOAD_PROJECT.service;

import project.OOAD_PROJECT.model.Assessment;
import project.OOAD_PROJECT.model.Question;
import project.OOAD_PROJECT.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    public Assessment createAssessment(Assessment assessment) {
        // Validate that there are no more than 15 questions
        if (assessment.getQuestions() != null && assessment.getQuestions().size() > 15) {
            throw new IllegalArgumentException("An assessment cannot have more than 15 questions");
        }
        
        // Set the assessment reference for each question
        if (assessment.getQuestions() != null) {
            for (Question question : assessment.getQuestions()) {
                question.setAssessment(assessment);
            }
        }
        
        return assessmentRepository.save(assessment);
    }

    public Optional<Assessment> getAssessmentByCourseAndModule(Long courseId, Long moduleId) {
        return assessmentRepository.findByCourseIdAndModuleId(courseId, moduleId);
    }

    public Assessment getAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));
    }

    public Assessment saveAssessment(Assessment assessment) {
        return assessmentRepository.save(assessment);
    }
} 