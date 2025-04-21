package project.OOAD_PROJECT.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.OOAD_PROJECT.model.PaidCourse;
import project.OOAD_PROJECT.model.PaidCourseModule;
import project.OOAD_PROJECT.repository.PaidCourseModuleRepository;
import project.OOAD_PROJECT.repository.PaidCourseRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PaidCourseModuleService {

    @Autowired
    private PaidCourseModuleRepository paidCourseModuleRepository;

    @Autowired
    private PaidCourseRepository paidCourseRepository;

    public PaidCourseModule addModuleToPaidCourse(Long courseId, PaidCourseModule module) {
        PaidCourse course = paidCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Paid Course not found with ID: " + courseId));
        module.setPaidCourse(course);
        return paidCourseModuleRepository.save(module);
    }

    public List<PaidCourseModule> listModulesByPaidCourse(Long courseId) {
        PaidCourse course = paidCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Paid Course not found with ID: " + courseId));
        return paidCourseModuleRepository.findByPaidCourse(course);
    }

    public void deleteModule(Long moduleId) {
        PaidCourseModule module = paidCourseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
        paidCourseModuleRepository.delete(module);
    }

    public boolean updateModule(Long moduleId, PaidCourseModule updatedModule) {
        Optional<PaidCourseModule> optional = paidCourseModuleRepository.findById(moduleId);
        if (optional.isPresent()) {
            PaidCourseModule existing = optional.get();
            existing.setTitle(updatedModule.getTitle());
            existing.setVideoUrl(updatedModule.getVideoUrl());
            paidCourseModuleRepository.save(existing);
            return true;
        }
        return false;
    }

    // public PaidCourseModule getModuleByCourseAndId(Long courseId, Long moduleId) {
    //     return paidCourseModuleRepository.findByIdAndPaidCourse_Id(moduleId, courseId)
    //         .orElseThrow(() -> new IllegalArgumentException("Module not found for given course ID"));
    // }

    public void printAllModules() {
        List<PaidCourseModule> all = paidCourseModuleRepository.findAll();
        for (PaidCourseModule m : all) {
            System.out.println("Module ID: " + m.getId()
                + ", Title: " + m.getTitle()
                + ", Course ID: " + (m.getPaidCourse() != null ? m.getPaidCourse().getId() : "null"));
        }
    }
    
    public PaidCourseModule getModuleByCourseAndId(Long courseId, Long moduleId) {
        System.out.println("Looking for module ID " + moduleId + " in course ID " + courseId);
        return paidCourseModuleRepository.findByIdAndPaidCourse_Id(moduleId, courseId)
                .orElse(null);
    }

    
    
}
