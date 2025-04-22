package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Route("take-test")
public class TakeTestView extends VerticalLayout implements BeforeEnterObserver {
    private final RestTemplate restTemplate = new RestTemplate();
    private Long courseId;
    private List<Map<String, Object>> questions = new ArrayList<>();
    private Map<Long, String> userAnswers = new HashMap<>();
    private Map<Long, Integer> moduleScores = new HashMap<>();

    public TakeTestView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        setPadding(true);
        setSpacing(true);
        getStyle().set("overflow-y", "auto");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            List<String> courseIdParams = event.getLocation().getQueryParameters().getParameters().get("courseId");
            if (courseIdParams == null || courseIdParams.isEmpty()) {
                Notification.show("Course ID is missing", 3000, Notification.Position.MIDDLE);
                UI.getCurrent().navigate("student-dashboard");
                return;
            }

            courseId = Long.parseLong(courseIdParams.get(0));
            loadQuestions();
        } catch (Exception e) {
            Notification.show("Error loading test: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            UI.getCurrent().navigate("student-dashboard");
        }
    }

    private void loadQuestions() {
        try {
            // Get all questions for the course
            String url = "http://localhost:8000/assessments/course/" + courseId + "/questions";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "success".equals(response.get("status"))) {
                questions = (List<Map<String, Object>>) response.get("questions");
                if (questions != null && !questions.isEmpty()) {
                    displayQuestions();
                } else {
                    Notification.show("No questions available for this course", 3000, Notification.Position.MIDDLE);
                    UI.getCurrent().navigate("student-dashboard");
                }
            } else {
                Notification.show("Failed to load questions: " + (response != null ? response.get("message") : "Unknown error"), 
                    3000, Notification.Position.MIDDLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error loading questions: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void displayQuestions() {
        removeAll();
        add(new H1("Course Assessment"));

        VerticalLayout questionsContainer = new VerticalLayout();
        questionsContainer.setWidth("80%");
        questionsContainer.setMaxWidth("800px");
        questionsContainer.setSpacing(true);
        questionsContainer.setPadding(true);

        for (Map<String, Object> question : questions) {
            try {
                // Safely get question ID with null check
                Object idObj = question.get("id");
                if (idObj == null) {
                    Notification.show("Invalid question format: missing ID", 3000, Notification.Position.MIDDLE);
                    continue;
                }
                Long questionId = ((Number) idObj).longValue();

                String questionText = (String) question.get("question");
                List<String> options = (List<String>) question.get("options");

                if (questionText == null || options == null) {
                    Notification.show("Invalid question format: missing question text or options", 3000, Notification.Position.MIDDLE);
                    continue;
                }

                VerticalLayout questionLayout = new VerticalLayout();
                questionLayout.setWidth("100%");
                questionLayout.getStyle().set("border", "1px solid #ccc");
                questionLayout.getStyle().set("padding", "16px");
                questionLayout.getStyle().set("margin", "8px 0");
                questionLayout.getStyle().set("border-radius", "4px");
                questionLayout.setSpacing(true);

                H3 questionHeader = new H3("Question");
                Div questionTextDiv = new Div();
                questionTextDiv.setText(questionText);
                questionTextDiv.getStyle().set("white-space", "normal");

                RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>();
                radioGroup.setItems(options);
                radioGroup.setWidth("100%");
                radioGroup.addValueChangeListener(e -> {
                    if (e.getValue() != null) {
                        userAnswers.put(questionId, e.getValue());
                    }
                });

                questionLayout.add(questionHeader, questionTextDiv, radioGroup);
                questionsContainer.add(questionLayout);
            } catch (Exception e) {
                Notification.show("Error displaying question: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
                continue;
            }
        }

        add(questionsContainer);

        Button submitButton = new Button("Submit Test", e -> submitTest());
        submitButton.getStyle().set("background-color", "#28a745").set("color", "white");
        add(submitButton);
    }

    private Map<String, List<Map<String, Object>>> groupQuestionsByTopic() {
        Map<String, List<Map<String, Object>>> topicGroups = new HashMap<>();
        
        // First pass: identify potential topics from the first few questions
        Set<String> potentialTopics = new HashSet<>();
        for (Map<String, Object> question : questions) {
            String questionText = (String) question.get("question");
            if (questionText != null) {
                // Look for technical terms or keywords
                String[] words = questionText.toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.length() > 3 && !word.matches("^(what|which|how|why|when|where|is|are|the|a|an)$")) {
                        potentialTopics.add(word);
                    }
                }
            }
        }

        // Second pass: group questions by their most relevant topic
        for (Map<String, Object> question : questions) {
            String questionText = (String) question.get("question");
            if (questionText != null) {
                String bestTopic = "General";
                int maxMatches = 0;

                for (String topic : potentialTopics) {
                    int matches = countWordOccurrences(questionText.toLowerCase(), topic);
                    if (matches > maxMatches) {
                        maxMatches = matches;
                        bestTopic = topic;
                    }
                }

                topicGroups.computeIfAbsent(bestTopic, k -> new ArrayList<>()).add(question);
            }
        }

        return topicGroups;
    }

    private int countWordOccurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }

    private void submitTest() {
        // Check if all questions have been answered
        boolean allAnswered = true;
        for (Map<String, Object> question : questions) {
            try {
                Long questionId = ((Number) question.get("id")).longValue();
                if (!userAnswers.containsKey(questionId)) {
                    allAnswered = false;
                    break;
                }
            } catch (Exception e) {
                allAnswered = false;
                break;
            }
        }

        if (!allAnswered) {
            Notification.show("Please answer all questions before submitting", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Calculate total score
        int totalQuestions = questions.size();
        int correctAnswers = 0;

        for (Map<String, Object> question : questions) {
            try {
                Long questionId = ((Number) question.get("id")).longValue();
                String correctAnswer = (String) question.get("correctAnswer");
                String userAnswer = userAnswers.get(questionId);

                if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                    correctAnswers++;
                }
            } catch (Exception e) {
                Notification.show("Error evaluating answer: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        }

        // Display results
        displayResults(totalQuestions, correctAnswers);
    }

    private void displayResults(int totalQuestions, int correctAnswers) {
        removeAll();
        add(new H1("Test Results"));

        double scorePercentage = (double) correctAnswers / totalQuestions * 100;

        Div scoreDiv = new Div();
        scoreDiv.setText(String.format("Your Score: %d/%d (%.2f%%)", correctAnswers, totalQuestions, scorePercentage));
        scoreDiv.getStyle().set("font-size", "24px").set("margin", "20px 0");

        Div feedbackDiv = new Div();
        if (scorePercentage >= 80) {
            feedbackDiv.setText("Excellent! You have a strong understanding of the course material.");
        } else if (scorePercentage >= 60) {
            feedbackDiv.setText("Good job! You have a good understanding of most topics.");
        } else {
            feedbackDiv.setText("Consider reviewing the course material to improve your understanding.");
        }
        feedbackDiv.getStyle().set("margin", "20px 0").set("font-weight", "bold");

        Button backButton = new Button("Back to Dashboard", e -> UI.getCurrent().navigate("student-dashboard"));
        backButton.getStyle().set("background-color", "#007bff").set("color", "white");

        add(scoreDiv, feedbackDiv, backButton);
    }
} 