package project.OOAD_PROJECT.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.OOAD_PROJECT.model.FreeCourse;
import project.OOAD_PROJECT.service.FreeCourseService;

import java.util.List;

@RestController
@RequestMapping("/free-courses")
public class FreeCourseController {

    private final FreeCourseService courseService;

    public FreeCourseController(FreeCourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/all")
    public List<FreeCourse> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PostMapping("/add")
    public String addCourse(@RequestBody FreeCourse course) {
    courseService.addCourse(course);
        return "Course added successfully!";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
    courseService.deleteCourse(id);
        return "Course deleted successfully!";
    }



}
