package project.OOAD_PROJECT.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.OOAD_PROJECT.model.FreeCourse;
import project.OOAD_PROJECT.repository.FreeCourseRepository;

import java.util.List;

@Service
public class FreeCourseService {

    private final FreeCourseRepository freeCourseRepository;

    @Autowired
    public FreeCourseService(FreeCourseRepository freeCourseRepository) {
        this.freeCourseRepository = freeCourseRepository;
    }

    public List<FreeCourse> getAllCourses() {
        return freeCourseRepository.getAllCourses();
    }

    public void addCourse(FreeCourse course) {
        freeCourseRepository.saveCourse(course);
    }
    
    public void deleteCourse(Long id) {
        freeCourseRepository.deleteCourseById(id);
    }
    
}
