//package project.OOAD_PROJECT;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Service;
//
//
//import java.util.*;
//
//@SpringBootApplication
//public class ElearningApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(ElearningApplication.class, args);
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
//
//@RestController
//@RequestMapping("/users")
//class UserController {
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/register")
//    public String register(@RequestBody User user) {
//        return userService.register(user);
//    }
//}
//
//
//@Service
//class UserService {
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//    private Map<String, String> users = new HashMap<>();
//
//    public String register(User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        users.put(user.getUsername(), user.getPassword());
//        return "User registered successfully";
//    }
//}
//
//@RestController
//@RequestMapping("/courses")
//class CourseController {
//    private List<String> courses = new ArrayList<>();
//
//    @PostMapping("/add")
//    public String addCourse(@RequestParam String course) {
//        courses.add(course);
//        return "Course added successfully";
//    }
//
//    @GetMapping("/list")
//    public List<String> listCourses() {
//        return courses;
//    }
//}
//
//@RestController
//@RequestMapping("/payments")
//class PaymentController {
//    @PostMapping("/pay")
//    public String processPayment(@RequestParam String course) {
//        return "Payment for " + course + " processed successfully";
//    }
//}
//
//@RestController
//@RequestMapping("/quizzes")
//class QuizController {
//    private Map<String, String> quizzes = new HashMap<>();
//
//    @PostMapping("/create")
//    public String createQuiz(@RequestParam String title, @RequestParam String answer) {
//        quizzes.put(title, answer);
//        return "Quiz created successfully";
//    }
//
//    @PostMapping("/attempt")
//    public String attemptQuiz(@RequestParam String title, @RequestParam String userAnswer) {
//        return quizzes.get(title).equals(userAnswer) ? "Correct answer!" : "Wrong answer!";
//    }
//}

package project.OOAD_PROJECT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.*;

@SpringBootApplication
public class ElearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElearningApplication.class, args);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
