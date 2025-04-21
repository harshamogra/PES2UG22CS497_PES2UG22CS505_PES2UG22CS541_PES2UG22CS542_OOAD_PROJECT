
package com.example.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.UI;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import elemental.json.JsonValue;
import elemental.json.JsonType;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Route("buy-courses")
// @StyleSheet("styles/shared-styles.css")
public class BuyPage extends VerticalLayout {

    private static final String COURSES_API_URL = "http://localhost:8000/paid-courses/list";
    private static final String CHECKOUT_SESSION_URL = "http://localhost:8000/payments/checkout";

    private Long userId;
    private String username;    // <-- Add this line
    private String userRole; 

    public BuyPage() {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        // Check localStorage for user data
        UI.getCurrent().getPage().executeJs("""
            if (!localStorage.getItem('user')) {
                localStorage.setItem('user', JSON.stringify({
                    id: 2,
                    username: 'student_testing',
                    role: 'STUDENT',
                    purchasedCourses: []
                }));
            }
        """);

        UI.getCurrent().getPage().executeJs("return window.localStorage.getItem('user');")
            .then(userJson -> {
                try {
                    if (userJson != null && userJson instanceof JsonValue jsonValue &&
                        jsonValue.getType() == JsonType.STRING &&
                        jsonValue.asString() != null && !jsonValue.asString().isEmpty()) {

                        String jsonString = jsonValue.asString();
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> userMap = mapper.readValue(jsonString, Map.class);

                        Object idObj = userMap.get("id");
                        Object roleObj = userMap.get("role");
                        Object usernameObj = userMap.get("username");

                        if (idObj instanceof Number) {
                            this.userId = ((Number) idObj).longValue();
                            this.username = (String) usernameObj;
                            this.userRole = String.valueOf(roleObj);
                            
                            // Load courses after user validation
                            loadCourses();
                        } else {
                            Notification.show("❌ Invalid user data");
                            UI.getCurrent().navigate("login");
                        }
                    } else {
                        Notification.show("❌ User not found in local storage");
                        UI.getCurrent().navigate("login");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Notification.show("❌ Error parsing user data");
                    UI.getCurrent().navigate("login");
                }
            });
    }

    private void loadCourses() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List<Course>> response = restTemplate.exchange(
                    COURSES_API_URL,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new org.springframework.core.ParameterizedTypeReference<List<Course>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Course> courses = response.getBody();
                displayCoursesInCards(courses);
            }
        } catch (HttpClientErrorException e) {
            Notification.show("❌ Error fetching courses: " + e.getMessage());
        }
    }

    private void displayCoursesInCards(List<Course> courses) {
        FlexLayout courseLayout = new FlexLayout();
        courseLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        courseLayout.setJustifyContentMode(JustifyContentMode.START);
        courseLayout.setWidthFull();
        courseLayout.getStyle().set("gap", "20px");

        for (Course course : courses) {
            Div courseCard = new Div();
            courseCard.setWidth("300px");
            courseCard.getStyle()
                    .set("border", "1px solid #ddd")
                    .set("border-radius", "10px")
                    .set("padding", "20px")
                    .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                    .set("background-color", "#fff");

            Span title = new Span(course.getTitle());
            title.getStyle()
                    .set("font-weight", "bold")
                    .set("font-size", "22px")
                    .set("color", "#2980b9");
            courseCard.add(title);

            Div modulesDiv = new Div();
            if (course.getModules() != null) {
                course.getModules().forEach(module -> {
                    Span moduleSpan = new Span(module.getName());
                    moduleSpan.getStyle()
                            .set("display", "block")
                            .set("font-size", "16px")
                            .set("margin-top", "10px")
                            .set("color", "#34495e");
                    modulesDiv.add(moduleSpan);
                });
            }
            courseCard.add(modulesDiv);

            Span price = new Span("Price: $" + course.getPrice());
            price.getStyle()
                    .set("margin-top", "10px")
                    .set("color", "green")
                    .set("font-weight", "bold");
            courseCard.add(price);

            Button buyButton = new Button("Buy Course", e -> {
                if (userId != null) {
                    createCheckoutSession(userId, course.getId(), course.getPrice());
                } else {
                    Notification.show("User not identified. Please log in again.");
                }
            });
            buyButton.getStyle()
                    .set("margin-top", "20px")
                    .set("background-color", "#27ae60")
                    .set("color", "white")
                    .set("border", "none")
                    .set("border-radius", "5px")
                    .set("padding", "10px");
            courseCard.add(buyButton);

            courseLayout.add(courseCard);
        }

        add(courseLayout);
    }

    private void createCheckoutSession(Long userId, Long courseId, Double amount) {
        RestTemplate restTemplate = new RestTemplate();
        
        CheckoutRequest requestBody = new CheckoutRequest(
            Math.round(amount),  // Convert to cents and ensure it's in Long
            "INR",
            "http://localhost:3000/buy-courses/success?session_id={CHECKOUT_SESSION_ID}",  // Stripe will replace this placeholder
            "http://localhost:3000/buy-courses/failed?session_id={CHECKOUT_SESSION_ID}"    // Stripe will replace this placeholder
        );
    
        try {
            ResponseEntity<CheckoutResponse> response = restTemplate.postForEntity(
                    CHECKOUT_SESSION_URL + "/" + userId + "/" + courseId,
                    requestBody,
                    CheckoutResponse.class
            );
    
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UI.getCurrent().getPage().executeJs(
                    "window.location.href = $0",
                    response.getBody().getCheckoutUrl()  // Note: changed from getUrl() to getCheckoutUrl()
                );
            } else {
                Notification.show("❌ Error creating checkout session");
            }
        } catch (Exception e) {
            Notification.show("❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update CheckoutResponse class to match backend
    public static class CheckoutResponse {
        private String checkoutUrl;
        public String getCheckoutUrl() { return checkoutUrl; }
    }

    // You must define User, Course, Module, CheckoutRequest, and CheckoutResponse classes here or import them

    public static class User {
        private Long id;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class Course {
        private Long id;
        private String title;
        private Double price;
        private List<Module> modules;

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public Double getPrice() { return price; }
        public List<Module> getModules() { return modules; }
    }

    public static class Module {
        private Long id;
        private String name;

        public Long getId() { return id; }
        public String getName() { return name; }
    }

    public static class CheckoutRequest {
        private Long amount;  // Changed from Double to Long
        private String currency;
        private String successUrl;  // Added
        private String cancelUrl;   // Added
    
        // Default constructor
        public CheckoutRequest() {}
    
        public CheckoutRequest(Long amount, String currency, String successUrl, String cancelUrl) {
            this.amount = amount;
            this.currency = currency;
            this.successUrl = successUrl;
            this.cancelUrl = cancelUrl;
        }
    
        // Getters and Setters
        public Long getAmount() { return amount; }
        public void setAmount(Long amount) { this.amount = amount; }
    
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    
        public String getSuccessUrl() { return successUrl; }
        public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
    
        public String getCancelUrl() { return cancelUrl; }
        public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
    }
}

