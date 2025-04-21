package project.OOAD_PROJECT.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import project.OOAD_PROJECT.model.Feedback;
import project.OOAD_PROJECT.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/ask")
    public String askQuestion(@RequestParam String question) {
        feedbackService.askQuestion(question);
        return "Question posted successfully";
    }

    @PostMapping("/answer")
    public String answerQuestion(@RequestParam Long id, @RequestParam String answer) {
        return feedbackService.answerQuestion(id, answer);
    }

    @GetMapping("/list")
    public List<Feedback> listQuestions() {
        return feedbackService.listQuestions();
    }
}
