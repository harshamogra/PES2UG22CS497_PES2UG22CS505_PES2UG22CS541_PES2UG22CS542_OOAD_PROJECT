package com.example.application;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Route("buy-courses/success")
public class PaymentSuccessPage extends VerticalLayout implements BeforeEnterObserver {
    private static final String SUCCESS_API_URL = "http://localhost:8000/payments/success";
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        String sessionId = queryParameters.getParameters().getOrDefault("session_id", List.of("")).get(0);
        
        if (sessionId != null) {
            processPayment(sessionId);
        } else {
            Notification.show("❌ Invalid session");
            UI.getCurrent().navigate("buy-courses");
        }
    }

    private void processPayment(String sessionId) {
        System.out.println("Processing payment with session ID: " + sessionId); // Debug log
        
        RestTemplate restTemplate = new RestTemplate();
        String fullUrl = SUCCESS_API_URL + "?session_id=" + sessionId;
        System.out.println("Sending request to: " + fullUrl); // Debug log
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                fullUrl,
                String.class
            );
            
            System.out.println("Response status: " + response.getStatusCode()); // Debug log
            System.out.println("Response body: " + response.getBody()); // Debug log

            if (response.getStatusCode().is2xxSuccessful()) {
                setupSuccessUI(response.getBody());
            } else {
                String errorMessage = "Payment verification failed with status: " + response.getStatusCode();
                System.err.println(errorMessage); // Debug log
                Notification.show("❌ " + errorMessage);
                UI.getCurrent().navigate("buy-courses");
            }
        } catch (Exception e) {
            String errorMessage = "Error processing payment: " + e.getMessage();
            System.err.println(errorMessage); // Debug log
            e.printStackTrace(); // Print full stack trace
            Notification.show("❌ " + errorMessage);
            UI.getCurrent().navigate("buy-courses");
        }
    }

    private void setupSuccessUI(String message) {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);

        H2 successMessage = new H2("✅ Payment Successful!");
        successMessage.getStyle().set("color", "#27ae60");

        H2 details = new H2(message);
        details.getStyle().set("color", "#2980b9");

        Button viewCoursesButton = new Button("View My Courses", e -> 
            UI.getCurrent().navigate("my-courses")
        );
        viewCoursesButton.getStyle()
            .set("background-color", "#27ae60")
            .set("color", "white")
            .set("padding", "10px 20px");

        add(successMessage, details, viewCoursesButton);
    }
}