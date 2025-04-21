package project.OOAD_PROJECT.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.OOAD_PROJECT.model.Feedback;
import project.OOAD_PROJECT.repository.FeedbackRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // Method to post a question
    public Feedback askQuestion(String question) {
        Feedback feedback = new Feedback(null, question, new ArrayList<>());
        return feedbackRepository.save(feedback);
    }

    // Method to add an answer to a question
    public String answerQuestion(Long id, String answer) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            feedback.getAnswers().add(answer);
            feedbackRepository.save(feedback);
            return "Answer added successfully";
        }
        return "Question not found";
    }

    // Method to get all feedback (questions and answers)
    public List<Feedback> listQuestions() {
        return feedbackRepository.findAll();
    }
}
