package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("course-review")
public class CourseReviewView extends VerticalLayout implements HasUrlParameter<String> {

    private final RestTemplate restTemplate = new RestTemplate();
    private Long userId;
    private String username;
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
                UI.getCurrent().navigate("student-dashboard");
            }
        } else {
            Notification.show("Course ID is required");
            UI.getCurrent().navigate("student-dashboard");
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

        this.username = (String) userMap.get("username");
        this.userId = ((Number) userMap.get("id")).longValue();

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
            e.printStackTrace();
            Notification.show("❌ Failed to load course details");
        }

        H1 heading = new H1("⭐ Review Course: " + (courseTitle != null ? courseTitle : ""));
        heading.getStyle().set("margin-top", "20px").set("color", "#333");
        add(heading);

        // Review form
        Div formContainer = new Div();
        formContainer.getStyle()
            .set("background-color", "#fff")
            .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
            .set("padding", "20px")
            .set("border-radius", "12px")
            .set("width", "600px")
            .set("margin", "20px auto");

        H3 formHeading = new H3("Share Your Experience");
        
        ComboBox<Integer> ratingField = new ComboBox<>("Rating (1-5 stars)");
        ratingField.setItems(1, 2, 3, 4, 5);
        ratingField.setWidthFull();
        ratingField.setRequired(true);
        
        TextArea reviewArea = new TextArea("Your Review");
        reviewArea.setWidthFull();
        reviewArea.setHeight("150px");
        reviewArea.setRequired(true);

        Button submitButton = new Button("Submit Review");
        submitButton.getStyle()
            .set("background-color", "#f39c12")
            .set("color", "white")
            .set("margin-top", "20px");

        submitButton.addClickListener(e -> {
            if (ratingField.getValue() == null || reviewArea.getValue().isEmpty()) {
                Notification.show("❌ Please fill in all fields");
                return;
            }

            try {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("studentId", userId);
                reviewData.put("courseId", courseId);
                reviewData.put("rating", ratingField.getValue());
                reviewData.put("comment", reviewArea.getValue());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(reviewData, headers);

                // Changed endpoint to match backend
                restTemplate.postForEntity(
                    "http://localhost:8000/api/reviews/submit", 
                    request, 
                    Map.class
                );

                Notification.show("✅ Review submitted successfully!");
                UI.getCurrent().navigate("student-dashboard");
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("❌ Error: " + ex.getMessage());
            }
        });

        formContainer.add(formHeading, ratingField, reviewArea, submitButton);
        add(formContainer);

        // Course reviews section
        Div reviewsContainer = new Div();
        reviewsContainer.setWidthFull();
        reviewsContainer.getStyle().set("margin-top", "30px");

        H3 reviewsHeading = new H3("Course Reviews");
        reviewsContainer.add(reviewsHeading);
        
        try {
            // Changed endpoint to match backend
            ResponseEntity<List> reviewsResponse = restTemplate.getForEntity(
                "http://localhost:8080/api/reviews/course/" + courseId, 
                List.class
            );
            
            List<Map<String, Object>> reviews = reviewsResponse.getBody();
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
        Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate("student-dashboard"));
        backButton.getStyle()
            .set("margin-top", "20px")
            .set("background-color", "#0077cc")
            .set("color", "white");
        add(backButton);
    }

    // Add this method at the end of the class, before the closing brace
    
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
        
        // Create star rating display
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("⭐");
        }
        
        H3 ratingHeading = new H3(stars.toString());
        Paragraph commentPara = new Paragraph(comment);
        Paragraph datePara = new Paragraph("📅 " + createdAt);
        datePara.getStyle().set("color", "#666").set("font-size", "0.8em");

        item.add(ratingHeading, commentPara, datePara);
        return item;
    }
}