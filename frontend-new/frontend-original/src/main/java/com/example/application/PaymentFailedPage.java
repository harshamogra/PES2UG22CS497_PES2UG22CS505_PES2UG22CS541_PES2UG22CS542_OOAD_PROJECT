package com.example.application;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.web.client.RestTemplate;

@Route("buy-courses/failed")
public class PaymentFailedPage extends VerticalLayout implements BeforeEnterObserver {
    private static final String FAILED_API_URL = "http://localhost:8000/payments/failed";
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);

        notifyBackend();
        setupFailedUI();
    }

    private void notifyBackend() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForEntity(FAILED_API_URL, String.class);
        } catch (Exception e) {
            Notification.show("❌ Error notifying backend: " + e.getMessage());
        }
    }

    private void setupFailedUI() {
        H2 failedMessage = new H2("❌ Payment Failed or Cancelled");
        failedMessage.getStyle().set("color", "#e74c3c");

        H2 details = new H2("Please try again or contact support if the problem persists.");
        details.getStyle().set("color", "#7f8c8d");

        Button retryButton = new Button("Return to Courses", e -> 
            UI.getCurrent().navigate("buy-courses")
        );
        retryButton.getStyle()
            .set("background-color", "#3498db")
            .set("color", "white")
            .set("padding", "10px 20px");

        add(failedMessage, details, retryButton);
    }
}