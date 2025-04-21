package project.OOAD_PROJECT.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class PaidCourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "paid_course_id")
    @JsonIgnore
    private PaidCourse paidCourse;

    // Constructors
    public PaidCourseModule() {}

    public PaidCourseModule(String title, String videoUrl) {
        this.title = title;
        this.videoUrl = videoUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public PaidCourse getPaidCourse() {
        return paidCourse;
    }

    public void setPaidCourse(PaidCourse paidCourse) {
        this.paidCourse = paidCourse;
    }
}
