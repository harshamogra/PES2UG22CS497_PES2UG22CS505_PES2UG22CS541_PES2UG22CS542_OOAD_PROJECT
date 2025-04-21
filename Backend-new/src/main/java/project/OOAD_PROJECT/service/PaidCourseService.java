package project.OOAD_PROJECT.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.OOAD_PROJECT.model.PaidCourse;
import project.OOAD_PROJECT.model.User;
import project.OOAD_PROJECT.repository.PaidCourseRepository;
import project.OOAD_PROJECT.repository.UserRepository;

import java.util.List;

@Service
public class PaidCourseService {

    @Autowired
    private PaidCourseRepository paidCourseRepository;

    @Autowired
    private UserRepository userRepository;  // ✅ Correctly injected

    // Save a new paid course (instructor must be set)
    public PaidCourse addPaidCourse(PaidCourse course) {
        if (course.getInstructor() == null) {
            throw new IllegalArgumentException("Instructor must be assigned to the course.");
        }
        return paidCourseRepository.save(course);
    }

    // Get all paid courses
    public List<PaidCourse> listPaidCourses() {
        return paidCourseRepository.findAll();
    }

    // Get all paid courses by instructor
    public List<PaidCourse> listCoursesByInstructor(Long instructorId) {
        return paidCourseRepository.findByInstructorId(instructorId);
    }

    // Delete course by ID
    public void deletePaidCourse(Long courseId) {
        PaidCourse course = paidCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Paid Course not found with ID: " + courseId));
        paidCourseRepository.delete(course);
    }

    // Optional: Delete course only if instructor matches
    public void deletePaidCourseByInstructor(Long courseId, Long instructorId) {
        PaidCourse course = paidCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Paid Course not found with ID: " + courseId));

        if (course.getInstructor() == null || !course.getInstructor().getId().equals(instructorId)) {
            throw new SecurityException("Instructor not authorized to delete this course.");
        }

        paidCourseRepository.delete(course);
    }

    // ✅ NEW: Get purchased courses by user ID
    public List<PaidCourse> getPurchasedCoursesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return user.getPurchasedCourses();
    }

    public PaidCourse updatePaidCourse(Long courseId, PaidCourse updatedCourse) {
        PaidCourse existingCourse = paidCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
    
        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());
        existingCourse.setPrice(updatedCourse.getPrice());
    
        // Optional: update instructor only if changed
        if (updatedCourse.getInstructor() != null) {
            User instructor = userRepository.findById(updatedCourse.getInstructor().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Instructor not found with ID: " + updatedCourse.getInstructor().getId()));
            existingCourse.setInstructor(instructor);
        }
    
        return paidCourseRepository.save(existingCourse);
    }

    public PaidCourse getById(Long id) {
        return paidCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No course found with ID: " + id));
    }
    
    
}
