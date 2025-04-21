package project.OOAD_PROJECT.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)  // ✅ Stores role as "INSTRUCTOR" or "STUDENT"
    @Column(nullable = false)
    private Role role; 

   
    @ManyToMany
    @JoinTable(
        name = "user_paid_courses",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "paid_course_id")
    )
    private List<PaidCourse> purchasedCourses = new ArrayList<>();

    // Constructors
    public User() {
        this.purchasedCourses = new ArrayList<>();
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.purchasedCourses = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<PaidCourse> getPurchasedCourses() {
        return purchasedCourses;
    }

    public void setPurchasedCourses(List<PaidCourse> purchasedCourses) {
        this.purchasedCourses = purchasedCourses;
    }

    // Relationship Methods
    public void purchaseCourse(PaidCourse course) {
        if (!purchasedCourses.contains(course)) { 
            purchasedCourses.add(course);
            course.getEnrolledUsers().add(this);
        }
    }

    public void removeCourse(PaidCourse course) {
        if (purchasedCourses.contains(course)) {
            purchasedCourses.remove(course);
            course.getEnrolledUsers().remove(this);
        }
    }
}
