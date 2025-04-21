package project.OOAD_PROJECT.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.OOAD_PROJECT.model.PaidCourseModule;
import project.OOAD_PROJECT.service.PaidCourseModuleService;

import java.util.List;

@RestController
@RequestMapping("/paid-course-modules")
public class PaidCourseModuleController {

    @Autowired
    private PaidCourseModuleService moduleService;

    // ✅ Add module to a paid course
    @PostMapping("/add/{courseId}")
    public ResponseEntity<?> addModule(@PathVariable Long courseId, @RequestBody PaidCourseModule module) {
        try {
            PaidCourseModule savedModule = moduleService.addModuleToPaidCourse(courseId, module);
            return ResponseEntity.ok(savedModule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to add module: " + e.getMessage());
        }
    }

    // ✅ List modules of a paid course
    @GetMapping("/list/{courseId}")
    public ResponseEntity<?> listModules(@PathVariable Long courseId) {
        try {
            List<PaidCourseModule> modules = moduleService.listModulesByPaidCourse(courseId);
            return ResponseEntity.ok(modules);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch modules: " + e.getMessage());
        }
    }

    // ✅ Delete a specific module
    @DeleteMapping("/delete/{moduleId}")
    public ResponseEntity<?> deleteModule(@PathVariable Long moduleId) {
        try {
            moduleService.deleteModule(moduleId);
            return ResponseEntity.ok("Module deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete module: " + e.getMessage());
        }
    }

    // ✅ Update an existing module
    @PutMapping("/update/{moduleId}")
    public ResponseEntity<?> updateModule(
            @PathVariable Long moduleId,
            @RequestBody PaidCourseModule updatedModule) {
        try {
            boolean success = moduleService.updateModule(moduleId, updatedModule);
            if (success) {
                return ResponseEntity.ok("Module updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update module: " + e.getMessage());
        }
    }

    @GetMapping("/{courseId}/{moduleId}")
    public ResponseEntity<?> getModuleById(
        @PathVariable Long courseId,
        @PathVariable Long moduleId) {
    try {
        PaidCourseModule module = moduleService.getModuleByCourseAndId(courseId, moduleId);
        if (module != null) {
            return ResponseEntity.ok(module);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found in course.");
        }
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("Failed to fetch module: " + e.getMessage());
    }
    }

    
}




