package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Route("course-reviews")
public class CourseReviewsView extends VerticalLayout implements HasUrlParameter<String> {

    private final RestTemplate restTemplate = new RestTemplate();
    private Long courseId;
    private String courseTitle;

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        if (params.containsKey("courseId")) {
            try {
                this.courseId = Long.parseLong(params.get("courseId").get(0));
                initView();
            } catch (NumberFormatException e) {
                Notification.show("Invalid course ID");
                UI.getCurrent().navigate("instructor-dashboard");
            }
        } else {
            Notification.show("Course ID is required");
            UI.getCurrent().navigate("instructor-dashboard");
        }
    }

    private void initView() {
        setSpacing(true);
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        // Get session user data
        Map<String, Object> userMap = (Map<String, Object>) VaadinSession.getCurrent().getAttribute("user");

        if (userMap == null) {
            Notification.show("You must log in first.");
            UI.getCurrent().navigate("login");
            return;
        }

        String role = (String) userMap.get("role");
        if (!"INSTRUCTOR".equals(role)) {
            Notification.show("❌ Only instructors can access this page.");
            UI.getCurrent().navigate("login");
            return;
        }

        // Fetch course details
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:8000/paid-courses/" + courseId, 
                Map.class
            );
            
            Map<String, Object> course = response.getBody();
            if (course != null) {
                this.courseTitle = (String) course.get("title");
            }
        } catch (Exception e) {
            this.courseTitle = "Course";
        }

        H1 heading = new H1("⭐ Reviews for: " + (courseTitle != null ? courseTitle : ""));
        heading.getStyle().set("margin-top", "20px").set("color", "#333");
        add(heading);

        // Course statistics
        Div statsContainer = new Div();
        statsContainer.getStyle()
            .set("background-color", "#fff")
            .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
            .set("padding", "20px")
            .set("border-radius", "12px")
            .set("width", "600px")
            .set("margin", "20px auto");
        
        try {
            // First try to get stats from the stats endpoint
            ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:8000/api/reviews/stats/" + courseId, 
                Map.class
            );
            
            Map<String, Object> stats = response.getBody();
            if (stats != null && stats.containsKey("averageRating")) {
                Double avgRating = stats.get("averageRating") != null ? 
                    ((Number) stats.get("averageRating")).doubleValue() : 0.0;
                Integer totalReviews = stats.get("totalReviews") != null ? 
                    ((Number) stats.get("totalReviews")).intValue() : 0;
                
                H3 avgRatingHeading = new H3("Average Rating: " + String.format("%.1f", avgRating) + " ⭐");
                Paragraph totalReviewsPara = new Paragraph("Total Reviews: " + totalReviews);
                
                statsContainer.add(avgRatingHeading, totalReviewsPara);
            } else {
                // If stats endpoint doesn't return proper data, calculate from reviews
                ResponseEntity<List> reviewsResponse = restTemplate.getForEntity(
                    "http://localhost:8000/api/reviews/course/" + courseId, 
                    List.class
                );
                
                List<Map<String, Object>> reviews = reviewsResponse.getBody();
                if (reviews != null && !reviews.isEmpty()) {
                    // Calculate average rating
                    double totalRating = 0;
                    for (Map<String, Object> review : reviews) {
                        Integer rating = ((Number) review.get("rating")).intValue();
                        totalRating += rating;
                    }
                    double avgRating = totalRating / reviews.size();
                    
                    H3 avgRatingHeading = new H3("Average Rating: " + String.format("%.1f", avgRating) + " ⭐");
                    Paragraph totalReviewsPara = new Paragraph("Total Reviews: " + reviews.size());
                    
                    statsContainer.add(avgRatingHeading, totalReviewsPara);
                } else {
                    statsContainer.add(new H3("No reviews yet"));
                }
            }
        } catch (Exception e) {
            // If both approaches fail, try one more time with reviews endpoint
            try {
                ResponseEntity<List> reviewsResponse = restTemplate.getForEntity(
                    "http://localhost:8000/api/reviews/course/" + courseId, 
                    List.class
                );
                
                List<Map<String, Object>> reviews = reviewsResponse.getBody();
                if (reviews != null && !reviews.isEmpty()) {
                    // Calculate average rating
                    double totalRating = 0;
                    for (Map<String, Object> review : reviews) {
                        Integer rating = ((Number) review.get("rating")).intValue();
                        totalRating += rating;
                    }
                    double avgRating = totalRating / reviews.size();
                    
                    H3 avgRatingHeading = new H3("Average Rating: " + String.format("%.1f", avgRating) + " ⭐");
                    Paragraph totalReviewsPara = new Paragraph("Total Reviews: " + reviews.size());
                    
                    statsContainer.add(avgRatingHeading, totalReviewsPara);
                } else {
                    statsContainer.add(new H3("No reviews yet"));
                }
            } catch (Exception ex) {
                statsContainer.add(new H3("No reviews yet"));
            }
        }
        add(statsContainer);

        // Reviews container
        Div reviewsContainer = new Div();
        reviewsContainer.setWidthFull();
        reviewsContainer.getStyle().set("margin-top", "20px");
        
        H3 reviewsHeading = new H3("All Reviews");
        reviewsContainer.add(reviewsHeading);
        
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                "http://localhost:8000/api/reviews/course/" + courseId, 
                List.class
            );
            
            List<Map<String, Object>> reviews = response.getBody();
            if (reviews != null && !reviews.isEmpty()) {
                for (Map<String, Object> review : reviews) {
                    reviewsContainer.add(createReviewItem(review));
                }
            } else {
                reviewsContainer.add(new Paragraph("No reviews yet for this course."));
            }
        } catch (Exception e) {
            reviewsContainer.add(new Paragraph("No reviews yet for this course."));
        }
        add(reviewsContainer);

        // Back button
        Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate("instructor-dashboard"));
        backButton.getStyle()
            .set("margin-top", "20px")
            .set("background-color", "#0077cc")
            .set("color", "white");
        add(backButton);
    }

    private Div createReviewItem(Map<String, Object> review) {
        Div item = new Div();
        item.getStyle()
            .set("background-color", "#fff")
            .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
            .set("padding", "15px")
            .set("border-radius", "8px")
            .set("margin-top", "10px")
            .set("width", "100%");

        Integer rating = ((Number) review.get("rating")).intValue();
        String comment = (String) review.get("comment");
        String createdAt = (String) review.get("createdAt");
        
        Map<String, Object> student = (Map<String, Object>) review.get("student");
        String studentName = student != null ? (String) student.get("username") : "Anonymous";

        // Create star rating display
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("⭐");
        }
        
        H3 ratingHeading = new H3(stars.toString());
        Paragraph studentPara = new Paragraph("👨‍🎓 " + studentName);
        Paragraph commentPara = new Paragraph(comment);
        Paragraph datePara = new Paragraph("📅 " + createdAt);
        datePara.getStyle().set("color", "#666").set("font-size", "0.8em");

        item.add(ratingHeading, studentPara, commentPara, datePara);
        return item;
    }
}