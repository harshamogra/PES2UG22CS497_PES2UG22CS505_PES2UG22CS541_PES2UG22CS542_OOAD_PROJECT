package com.example.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Route("add-paid-course")
public class AddCourseView extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    public AddCourseView() {
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        // UI Elements
        H2 heading = new H2("➕ Add New Paid Course");
        TextField nameField = new TextField("Course Name");
        TextArea descField = new TextArea("Description");
        NumberField priceField = new NumberField("Price (₹)");
        priceField.setStep(0.01);
        priceField.setPlaceholder("e.g. 499.00");

        Button submitButton = new Button("Create Course");
        submitButton.getStyle().set("background-color", "#0077cc").set("color", "white");

        add(heading, nameField, descField, priceField, submitButton);

        // Read user from localStorage
        UI.getCurrent().getPage().executeJs("return localStorage.getItem('user');")
            .then(userJson -> {
                try {
                    if (userJson instanceof JsonValue jsonValue &&
                        jsonValue.getType() == JsonType.STRING &&
                        jsonValue.asString() != null && !jsonValue.asString().isEmpty()) {

                        String jsonString = jsonValue.asString();
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> userMap = mapper.readValue(jsonString, Map.class);

                        Object idObj = userMap.get("id");
                        Object roleObj = userMap.get("role");

                        if (idObj instanceof Number && "INSTRUCTOR".equals(String.valueOf(roleObj))) {
                            Long instructorId = ((Number) idObj).longValue();

                            // Add course on click
                            submitButton.addClickListener(event -> {
                                String title = nameField.getValue().trim();
                                String description = descField.getValue().trim();
                                Double price = priceField.getValue();
                            
                                // Clear previous invalid states
                                nameField.setInvalid(false);
                                descField.setInvalid(false);
                                priceField.setInvalid(false);
                            
                                // Validation flags
                                boolean valid = true;
                            
                                if (title.isEmpty()) {
                                    nameField.setInvalid(true);
                                    nameField.setErrorMessage("Course name is required");
                                    valid = false;
                                }
                            
                                if (description.isEmpty()) {
                                    descField.setInvalid(true);
                                    descField.setErrorMessage("Description is required");
                                    valid = false;
                                }
                            
                                if (price == null) {
                                    priceField.setInvalid(true);
                                    priceField.setErrorMessage("Price is required");
                                    valid = false;
                                }
                            
                                if (!valid) {
                                    Notification.show("⚠️ Please fill in all required fields.");
                                    return;
                                }
                            
                                // Build instructor object
                                Map<String, Object> instructor = new HashMap<>();
                                instructor.put("id", instructorId);
                            
                                Map<String, Object> course = new HashMap<>();
                                course.put("title", title);
                                course.put("description", description);
                                course.put("price", price);
                                course.put("instructor", instructor);
                            
                                try {
                                    ResponseEntity<Map> response = restTemplate.postForEntity(
                                            "http://localhost:8000/paid-courses/add", course, Map.class
                                    );
                            
                                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                                        Integer courseId = (Integer) response.getBody().get("id");
                                        Notification.show("✅ Course created!");
                                        UI.getCurrent().navigate("add-module?courseId=" + courseId);
                                    } else {
                                        Notification.show("❌ Failed to create course. Try again.");
                                    }
                            
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Notification.show("❌ Error creating course.");
                                }
                            });
                            

                        } else {
                            add(new Paragraph("❌ You must be logged in as an INSTRUCTOR to create a course."));
                            submitButton.setEnabled(false);
                        }

                    } else {
                        add(new Paragraph("❌ User data not found in localStorage."));
                        submitButton.setEnabled(false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    add(new Paragraph("❌ Error reading user data."));
                    submitButton.setEnabled(false);
                }
            });
    }
}
