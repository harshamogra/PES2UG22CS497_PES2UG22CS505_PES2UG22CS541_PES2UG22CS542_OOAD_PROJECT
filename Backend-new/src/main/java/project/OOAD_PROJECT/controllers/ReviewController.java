package project.OOAD_PROJECT.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private List<Map<String, Object>> reviews = new ArrayList<>();
    private long nextId = 1;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitReview(@RequestBody Map<String, Object> reviewData) {
        // Add an ID and timestamp to the review
        reviewData.put("id", nextId++);
        reviewData.put("createdAt", java.time.LocalDateTime.now().toString());
        
        // Store the review
        reviews.add(reviewData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review submitted successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Map<String, Object>>> getCourseReviews(@PathVariable Long courseId) {
        List<Map<String, Object>> courseReviews = new ArrayList<>();
        
        for (Map<String, Object> review : reviews) {
            Long reviewCourseId = ((Number) review.get("courseId")).longValue();
            if (reviewCourseId.equals(courseId)) {
                courseReviews.add(review);
            }
        }
        
        return ResponseEntity.ok(courseReviews);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Map<String, Object>>> getStudentReviews(@PathVariable Long studentId) {
        List<Map<String, Object>> studentReviews = new ArrayList<>();
        
        for (Map<String, Object> review : reviews) {
            Long reviewStudentId = ((Number) review.get("studentId")).longValue();
            if (reviewStudentId.equals(studentId)) {
                studentReviews.add(review);
            }
        }
        
        return ResponseEntity.ok(studentReviews);
    }
}