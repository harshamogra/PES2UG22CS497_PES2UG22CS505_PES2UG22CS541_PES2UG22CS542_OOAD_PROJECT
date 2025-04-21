package project.OOAD_PROJECT.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    private Map<String, String> quizzes = new HashMap<>();
    
    @PostMapping("/create")
    public String createQuiz(@RequestParam String title, @RequestParam String answer) {
        quizzes.put(title, answer);
        return "Quiz created successfully";
    }
    
    @PostMapping("/attempt")
    public String attemptQuiz(@RequestParam String title, @RequestParam String userAnswer) {
        return quizzes.get(title).equals(userAnswer) ? "Correct answer!" : "Wrong answer!";
    }
}
