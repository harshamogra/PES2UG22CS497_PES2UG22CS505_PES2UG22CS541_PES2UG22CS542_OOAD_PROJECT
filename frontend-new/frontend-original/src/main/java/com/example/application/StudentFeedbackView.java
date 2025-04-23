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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route("student-feedback")
@PageTitle("Student Feedback")
public class StudentFeedbackView extends VerticalLayout {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private VerticalLayout feedbackContainer;
    
    public StudentFeedbackView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);
        setSpacing(true);
        
        H1 title = new H1("Student Feedback");
        title.getStyle().set("margin-top", "20px");
        
        // Create feedback form
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setWidth("80%");
        formLayout.setMaxWidth("800px");
        formLayout.setPadding(true);
        formLayout.getStyle()
            .set("background-color", "#f8f9fa")
            .set("border-radius", "8px")
            .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.1)");
        
        H3 formTitle = new H3("Submit Your Question or Feedback");
        
        TextArea questionField = new TextArea("Your Question");
        questionField.setWidth("100%");
        questionField.setMinHeight("150px");
        questionField.setPlaceholder("Type your question or feedback here...");
        
        Button submitButton = new Button("Submit Feedback", e -> {
            if (questionField.getValue().trim().isEmpty()) {
                Notification.show("Please enter your question or feedback", 3000, Notification.Position.MIDDLE);
                return;
            }
            
            try {
                String url = "http://localhost:8000/feedback/ask?question=" + questionField.getValue().trim();
                restTemplate.postForObject(url, null, String.class);
                
                Notification.show("Feedback submitted successfully", 3000, Notification.Position.MIDDLE);
                questionField.clear();
                loadFeedback();
            } catch (Exception ex) {
                Notification.show("Error submitting feedback: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        submitButton.getStyle()
            .set("background-color", "#0077cc")
            .set("color", "white")
            .set("margin-top", "10px");
        
        formLayout.add(formTitle, questionField, submitButton);
        
        // Feedback display section
        H3 feedbackTitle = new H3("Previous Questions and Answers");
        feedbackContainer = new VerticalLayout();
        feedbackContainer.setWidth("80%");
        feedbackContainer.setMaxWidth("800px");
        
        Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate("student-dashboard"));
        backButton.getStyle()
            .set("margin-top", "20px")
            .set("background-color", "#6c757d")
            .set("color", "white");
        
        add(title, formLayout, feedbackTitle, feedbackContainer, backButton);
        
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
            .set("padding", "15px")
            .set("margin-bottom", "15px")
            .set("border-radius", "8px")
            .set("background-color", "white")
            .set("box-shadow", "0 2px 5px rgba(0, 0, 0, 0.1)");
        
        String question = (String) feedback.get("question");
        List<String> answers = (List<String>) feedback.get("answers");
        
        H3 questionHeader = new H3("Question:");
        questionHeader.getStyle().set("margin-top", "0");
        
        Paragraph questionText = new Paragraph(question);
        questionText.getStyle()
            .set("font-size", "16px")
            .set("margin-bottom", "15px");
        
        card.add(questionHeader, questionText);
        
        if (answers != null && !answers.isEmpty()) {
            H3 answersHeader = new H3("Answers:");
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
        } else {
            Paragraph noAnswer = new Paragraph("No answers yet.");
            noAnswer.getStyle().set("font-style", "italic").set("color", "#6c757d");
            card.add(noAnswer);
        }
        
        return card;
    }
}