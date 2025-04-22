package project.OOAD_PROJECT.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;
import project.OOAD_PROJECT.model.Question;
import project.OOAD_PROJECT.model.Assessment;
import project.OOAD_PROJECT.repository.AssessmentRepository;
import project.OOAD_PROJECT.repository.QuestionRepository;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {
    
    @Autowired
    private AssessmentRepository assessmentRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createAssessment(
            @RequestParam Long courseId,
            @RequestParam Long moduleId,
            @RequestBody List<Map<String, Object>> questions) {
        try {
            // Validate the questions
            if (questions == null || questions.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Assessment must contain at least one question"
                ));
            }
            
            // Find or create assessment
            Assessment assessment = assessmentRepository.findByCourseIdAndModuleId(courseId, moduleId)
                .orElse(new Assessment());
            
            assessment.setCourseId(courseId);
            assessment.setModuleId(moduleId);
            assessment = assessmentRepository.save(assessment);
            
            // Clear existing questions
            questionRepository.deleteByAssessmentId(assessment.getId());
            
            // Create and save new questions
            List<Question> questionEntities = new ArrayList<>();
            for (Map<String, Object> questionData : questions) {
                Question question = new Question();
                question.setAssessment(assessment);
                question.setText((String) questionData.get("question"));
                question.setOption1((String) ((List<?>) questionData.get("options")).get(0));
                question.setOption2((String) ((List<?>) questionData.get("options")).get(1));
                question.setOption3((String) ((List<?>) questionData.get("options")).get(2));
                question.setOption4((String) ((List<?>) questionData.get("options")).get(3));
                question.setCorrectOption(getCorrectOptionIndex((String) questionData.get("correctAnswer"), 
                    (List<String>) questionData.get("options")));
                questionEntities.add(question);
            }
            
            questionRepository.saveAll(questionEntities);
            
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Assessment created successfully",
                "courseId", courseId,
                "moduleId", moduleId,
                "questionCount", questions.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to create assessment: " + e.getMessage()
            ));
        }
    }
    
    private int getCorrectOptionIndex(String correctAnswer, List<String> options) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).equals(correctAnswer)) {
                return i + 1; // 1-based index
            }
        }
        return 1; // Default to first option if not found
    }
    
    @GetMapping("/course/{courseId}/questions")
    public ResponseEntity<?> getQuestionsForCourse(@PathVariable Long courseId) {
        try {
            List<Assessment> assessments = assessmentRepository.findByCourseId(courseId);
            List<Map<String, Object>> allQuestions = new ArrayList<>();
            
            for (Assessment assessment : assessments) {
                List<Question> questions = questionRepository.findByAssessmentId(assessment.getId());
                for (Question question : questions) {
                    Map<String, Object> q = new HashMap<>();
                    q.put("id", question.getId());
                    q.put("question", question.getText());
                    q.put("options", Arrays.asList(
                        question.getOption1(),
                        question.getOption2(),
                        question.getOption3(),
                        question.getOption4()
                    ));
                    q.put("correctAnswer", getCorrectAnswer(question));
                    allQuestions.add(q);
                }
            }
            
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Questions retrieved successfully",
                "totalQuestions", allQuestions.size(),
                "questions", allQuestions
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to retrieve questions: " + e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/delete/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        try {
            questionRepository.deleteById(questionId);
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Question deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to delete question: " + e.getMessage()
            ));
        }
    }
    
    private String getCorrectAnswer(Question question) {
        switch (question.getCorrectOption()) {
            case 1: return question.getOption1();
            case 2: return question.getOption2();
            case 3: return question.getOption3();
            case 4: return question.getOption4();
            default: return question.getOption1();
        }
    }
} 