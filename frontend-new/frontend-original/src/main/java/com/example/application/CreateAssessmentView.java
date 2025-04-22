package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@PageTitle("Create Assessment")
@Route("create-assessment")
public class CreateAssessmentView extends VerticalLayout implements BeforeEnterObserver {
    private final RestTemplate restTemplate = new RestTemplate();
    private ComboBox<Map<String, Object>> moduleComboBox;
    private List<Map<String, Object>> questions = new ArrayList<>();
    private VerticalLayout questionsLayout;
    private Long courseId;
    private Map<String, Object> course;

    public CreateAssessmentView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        setSpacing(true);
        getStyle().set("overflow-y", "auto");

        H1 header = new H1("Create Assessment");
        add(header);

        // Module selection
        moduleComboBox = new ComboBox<>("Select Module");
        moduleComboBox.setWidth("80%");
        moduleComboBox.setMaxWidth("800px");
        moduleComboBox.setItemLabelGenerator(module -> (String) module.get("title"));

        // Questions layout
        questionsLayout = new VerticalLayout();
        questionsLayout.setWidth("80%");
        questionsLayout.setMaxWidth("800px");
        questionsLayout.setSpacing(true);
        questionsLayout.setPadding(true);
        questionsLayout.getStyle().set("overflow-y", "auto");

        // Add question button
        Button addQuestionButton = new Button("Add Question", e -> addQuestion());
        addQuestionButton.setEnabled(false);

        // Back button
        Button backButton = new Button("Back to Course", e -> UI.getCurrent().navigate("instructor-dashboard"));

        // Enable buttons when module is selected
        moduleComboBox.addValueChangeListener(e -> {
            boolean enabled = e.getValue() != null;
            addQuestionButton.setEnabled(enabled);
            if (enabled) {
                loadExistingQuestions();
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(addQuestionButton, backButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("80%");
        buttonLayout.setMaxWidth("800px");

        add(moduleComboBox, questionsLayout, buttonLayout);
    }

    private void loadExistingQuestions() {
        try {
            // Get all questions for the course
            String url = "http://localhost:8000/assessments/course/" + courseId + "/questions";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "success".equals(response.get("status"))) {
                List<Map<String, Object>> existingQuestions = (List<Map<String, Object>>) response.get("questions");
                if (existingQuestions != null && !existingQuestions.isEmpty()) {
                    questions.clear();
                    questions.addAll(existingQuestions);
                    updateQuestionsLayout();
                }
            }
        } catch (Exception e) {
            Notification.show("Error loading existing questions: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            List<String> courseIdParams = event.getLocation().getQueryParameters().getParameters().get("courseId");
            if (courseIdParams == null || courseIdParams.isEmpty()) {
                Notification.show("Course ID is missing", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("instructor-dashboard");
                return;
            }

            String idParam = courseIdParams.get(0);
            courseId = Long.parseLong(idParam);
            
            // Fetch course details
            String url = "http://localhost:8000/paid-courses/" + courseId;
            course = restTemplate.getForObject(url, Map.class);
            
            if (course != null && course.containsKey("modules")) {
                List<Map<String, Object>> modules = (List<Map<String, Object>>) course.get("modules");
                moduleComboBox.setItems(modules);
            } else {
                Notification.show("Course not found or has no modules", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("instructor-dashboard");
            }
        } catch (NumberFormatException e) {
            Notification.show("Invalid course ID format", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("instructor-dashboard");
        } catch (Exception e) {
            Notification.show("Error loading course: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("instructor-dashboard");
        }
    }

    private void addQuestion() {
        FormLayout questionForm = new FormLayout();
        questionForm.setWidth("100%");
        questionForm.getStyle().set("padding", "16px");
        questionForm.getStyle().set("background-color", "#f8f9fa");
        questionForm.getStyle().set("border-radius", "4px");
        questionForm.getStyle().set("margin", "8px 0");

        TextField questionField = new TextField("Question");
        questionField.setWidth("100%");
        TextField option1Field = new TextField("Option 1");
        option1Field.setWidth("100%");
        TextField option2Field = new TextField("Option 2");
        option2Field.setWidth("100%");
        TextField option3Field = new TextField("Option 3");
        option3Field.setWidth("100%");
        TextField option4Field = new TextField("Option 4");
        option4Field.setWidth("100%");
        ComboBox<String> correctAnswerField = new ComboBox<>("Correct Answer");
        correctAnswerField.setWidth("100%");
        correctAnswerField.setItems("Option 1", "Option 2", "Option 3", "Option 4");

        Button saveButton = new Button("Save Question", e -> {
            if (questionField.getValue().isEmpty() || 
                option1Field.getValue().isEmpty() || 
                option2Field.getValue().isEmpty() || 
                option3Field.getValue().isEmpty() || 
                option4Field.getValue().isEmpty() || 
                correctAnswerField.getValue() == null) {
                Notification.show("Please fill all fields", 3000, Notification.Position.MIDDLE);
                return;
            }

            try {
                Map<String, Object> module = moduleComboBox.getValue();
                Map<String, Object> question = new HashMap<>();
                question.put("id", UUID.randomUUID().toString());
                question.put("question", questionField.getValue());
                question.put("options", Arrays.asList(
                    option1Field.getValue(),
                    option2Field.getValue(),
                    option3Field.getValue(),
                    option4Field.getValue()
                ));
                
                // Set correct answer based on selected option
                String selectedOption = correctAnswerField.getValue();
                String correctAnswer = "";
                switch(selectedOption) {
                    case "Option 1": correctAnswer = option1Field.getValue(); break;
                    case "Option 2": correctAnswer = option2Field.getValue(); break;
                    case "Option 3": correctAnswer = option3Field.getValue(); break;
                    case "Option 4": correctAnswer = option4Field.getValue(); break;
                }
                question.put("correctAnswer", correctAnswer);
                
                // Add question to the list
                questions.add(question);
                
                // Save all questions to backend
                String url = "http://localhost:8000/assessments/create?courseId=" + courseId + "&moduleId=" + module.get("id");
                Map<String, Object> response = restTemplate.postForObject(url, questions, Map.class);
                
                if (response != null && "success".equals(response.get("status"))) {
                    updateQuestionsLayout();
                    Notification.show("Question added successfully", 3000, Notification.Position.MIDDLE);
                    
                    // Clear form
                    questionField.clear();
                    option1Field.clear();
                    option2Field.clear();
                    option3Field.clear();
                    option4Field.clear();
                    correctAnswerField.clear();
                } else {
                    // Remove the question if save failed
                    questions.remove(question);
                    Notification.show("Failed to add question", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception ex) {
                Notification.show("Error adding question: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });

        questionForm.add(questionField, option1Field, option2Field, option3Field, option4Field, correctAnswerField, saveButton);
        questionsLayout.add(questionForm);
    }

    private void updateQuestionsLayout() {
        questionsLayout.removeAll();
        for (Map<String, Object> question : questions) {
            VerticalLayout questionContainer = new VerticalLayout();
            questionContainer.setWidth("100%");
            questionContainer.getStyle().set("border", "1px solid #ccc");
            questionContainer.getStyle().set("padding", "16px");
            questionContainer.getStyle().set("margin", "8px 0");
            questionContainer.getStyle().set("border-radius", "4px");
            questionContainer.getStyle().set("background-color", "#f8f9fa");
            
            H3 questionHeader = new H3("Question");
            Div questionText = new Div();
            questionText.setText((String) question.get("question"));
            questionText.getStyle().set("white-space", "normal");
            
            List<String> options = (List<String>) question.get("options");
            VerticalLayout optionsLayout = new VerticalLayout();
            optionsLayout.setSpacing(true);
            for (int i = 0; i < options.size(); i++) {
                Div optionText = new Div();
                optionText.setText("Option " + (i + 1) + ": " + options.get(i));
                optionText.getStyle().set("white-space", "normal");
                optionsLayout.add(optionText);
            }
            
            Button deleteButton = new Button("Delete Question", e -> deleteQuestion(question));
            deleteButton.getStyle().set("background-color", "#ff4444");
            deleteButton.getStyle().set("color", "white");
            
            questionContainer.add(questionHeader, questionText, optionsLayout, deleteButton);
            questionsLayout.add(questionContainer);
        }
    }

    private void deleteQuestion(Map<String, Object> questionToDelete) {
        try {
            Long questionId = ((Number) questionToDelete.get("id")).longValue();
            
            // Delete the question from backend
            String url = "http://localhost:8000/assessments/delete/" + questionId;
            restTemplate.delete(url);
            
            // Remove question from the list
            questions.remove(questionToDelete);
            updateQuestionsLayout();
            Notification.show("Question deleted successfully", 3000, Notification.Position.MIDDLE);
        } catch (Exception e) {
            Notification.show("Error deleting question: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
} 