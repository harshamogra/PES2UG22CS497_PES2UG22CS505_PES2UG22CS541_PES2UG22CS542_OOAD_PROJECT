package project.OOAD_PROJECT.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.OOAD_PROJECT.model.PaidCourse;
import project.OOAD_PROJECT.model.User;
import project.OOAD_PROJECT.repository.UserRepository;
import project.OOAD_PROJECT.service.PaidCourseService;

import java.util.List;

@RestController
@RequestMapping("/paid-courses")
public class PaidCourseController {

    @Autowired
    private PaidCourseService paidCourseService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addPaidCourse(@RequestBody PaidCourse course) {
        try {
            Long instructorId = course.getInstructor().getId();
            User instructor = userRepository.findById(instructorId)
                    .orElseThrow(() -> new IllegalArgumentException("Instructor not found"));

            course.setInstructor(instructor); // Reattach persistent instructor entity

            PaidCourse saved = paidCourseService.addPaidCourse(course);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating course: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public List<PaidCourse> listPaidCourses() {
        return paidCourseService.listPaidCourses();
    }

    @GetMapping("/purchased/{userId}")
    public ResponseEntity<?> getPurchasedCourses(@PathVariable Long userId) {
        try {
            List<PaidCourse> purchasedCourses = paidCourseService.getPurchasedCoursesByUserId(userId);
            return ResponseEntity.ok(purchasedCourses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<?> getCoursesByInstructor(@PathVariable Long instructorId) {
        try {
            List<PaidCourse> courses = paidCourseService.listCoursesByInstructor(instructorId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch courses: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<?> deletePaidCourse(@PathVariable Long courseId) {
        try {
            paidCourseService.deletePaidCourse(courseId);
            return ResponseEntity.ok("Paid Course and its modules deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Paid Course not found with ID: " + courseId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete Paid Course: " + e.getMessage());
        }
    }

    // Replaces the old PUT mapping
    @PostMapping("/update/{courseId}")
    public ResponseEntity<?> updatePaidCourse(@PathVariable Long courseId, @RequestBody PaidCourse updatedCourse) {
    try {
        PaidCourse course = paidCourseService.updatePaidCourse(courseId, updatedCourse);
        return ResponseEntity.ok(course);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Course not found: " + e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("Failed to update course: " + e.getMessage());
    }
}


    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
    try {
        PaidCourse course = paidCourseService.getById(id);
        return ResponseEntity.ok(course);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Course not found: " + e.getMessage());
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("Error retrieving course: " + e.getMessage());
    }
}



}
