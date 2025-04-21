package project.OOAD_PROJECT.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class PaidCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnoreProperties({"purchasedCourses", "password", "enrolledCourses"})
    private User instructor;

    @ManyToMany(mappedBy = "purchasedCourses")
    @JsonIgnore
    private List<User> enrolledUsers = new ArrayList<>();

    @OneToMany(mappedBy = "paidCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaidCourseModule> modules = new ArrayList<>();

    // Constructors
    public PaidCourse() {}

    public PaidCourse(String title, String description, Double price, User instructor) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.instructor = instructor;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public List<User> getEnrolledUsers() {
        return enrolledUsers;
    }

    public void setEnrolledUsers(List<User> enrolledUsers) {
        this.enrolledUsers = enrolledUsers;
    }

    public List<PaidCourseModule> getModules() {
        return modules;
    }

    public void setModules(List<PaidCourseModule> modules) {
        this.modules = modules;
    }

    public void addModule(PaidCourseModule module) {
        modules.add(module);
        module.setPaidCourse(this);
    }

    public void removeModule(PaidCourseModule module) {
        modules.remove(module);
        module.setPaidCourse(null);
    }
}
