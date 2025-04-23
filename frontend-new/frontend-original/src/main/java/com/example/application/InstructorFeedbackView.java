package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Route("instructor-feedback")
@PageTitle("Instructor Feedback Management")
public class InstructorFeedbackView extends VerticalLayout {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private VerticalLayout feedbackContainer;
    
    public InstructorFeedbackView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);
        
        H1 title = new H1("Manage Student Feedback");
        title.getStyle().set("margin-top", "20px");
        
        // Feedback display section
        feedbackContainer = new VerticalLayout();
        feedbackContainer.setWidth("80%");
        feedbackContainer.setMaxWidth("800px");
        
        Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate("instructor-dashboard"));
        backButton.getStyle()
            .set("margin-top", "20px")
            .set("background-color", "#6c757d")
            .set("color", "white");
        
        add(title, feedbackContainer, backButton);
        
        // Load existing feedback
        loadFeedback();
    }
    
    private void loadFeedback() {
        try {
            feedbackContainer.removeAll();
            
            List<Map<String, Object>> feedbackList = restTemplate.getForObject(
                "http://localhost:8000/feedback/list", List.class);
            
            if (feedbackList == null || feedbackList.isEmpty()) {
                feedbackContainer.add(new Paragraph("No feedback available yet."));
                return;
            }
            
            for (Map<String, Object> feedback : feedbackList) {
                Div feedbackCard = createFeedbackCard(feedback);
                feedbackContainer.add(feedbackCard);
            }
            
        } catch (Exception e) {
            Notification.show("Error loading feedback: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
    
    private Div createFeedbackCard(Map<String, Object> feedback) {
        Div card = new Div();
        card.getStyle()
            .set("width", "100%")
            .set("padding", "20px")
            .set("margin-bottom", "20px")
            .set("border-radius", "8px")
            .set("background-color", "white")
            .set("box-shadow", "0 2px 5px rgba(0, 0, 0, 0.1)");
        
        Long id = ((Number) feedback.get("id")).longValue();
        String question = (String) feedback.get("question");
        List<String> answers = (List<String>) feedback.get("answers");
        
        H3 questionHeader = new H3("Question:");
        questionHeader.getStyle().set("margin-top", "0");
        
        Paragraph questionText = new Paragraph(question);
        questionText.getStyle()
            .set("font-size", "16px")
            .set("margin-bottom", "15px")
            .set("font-weight", "500");
        
        card.add(questionHeader, questionText);
        
        if (answers != null && !answers.isEmpty()) {
            H3 answersHeader = new H3("Previous Answers:");
            card.add(answersHeader);
            
            for (String answer : answers) {
                Paragraph answerText = new Paragraph(answer);
                answerText.getStyle()
                    .set("background-color", "#f0f7ff")
                    .set("padding", "10px")
                    .set("border-radius", "5px")
                    .set("margin-bottom", "10px");
                
                card.add(answerText);
            }
        }
        
        // Answer form
        H3 replyHeader = new H3("Add Your Answer:");
        TextArea answerField = new TextArea();
        answerField.setWidth("100%");
        answerField.setMinHeight("100px");
        answerField.setPlaceholder("Type your answer here...");
        
        Button submitButton = new Button("Submit Answer", e -> {
            if (answerField.getValue().trim().isEmpty()) {
                Notification.show("Please enter your answer", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            try {
                String url = "http://localhost:8000/feedback/answer?id=" + id + "&answer=" + answerField.getValue().trim();
                String response = restTemplate.postForObject(url, null, String.class);
                
                Notification.show("Answer submitted successfully", 3000, Notification.Position.MIDDLE);
                answerField.clear();
                loadFeedback();
            } catch (Exception ex) {
                Notification.show("Error submitting answer: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        submitButton.getStyle()
            .set("background-color", "#28a745")
            .set("color", "white")
            .set("margin-top", "10px");
        
        card.add(replyHeader, answerField, submitButton);
        
        return card;
    }
}