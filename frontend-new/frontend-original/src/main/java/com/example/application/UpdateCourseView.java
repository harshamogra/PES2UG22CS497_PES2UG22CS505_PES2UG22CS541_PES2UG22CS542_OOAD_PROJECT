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
import com.vaadin.flow.router.*;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Route("update-course")
public class UpdateCourseView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RestTemplate restTemplate = new RestTemplate();

    private TextField nameField = new TextField("Course Name");
    private TextArea descField = new TextArea("Description");
    private NumberField priceField = new NumberField("Price (₹)");
    private Button updateButton = new Button("💾 Update Course");

    private Long courseId;
    private Long instructorId;

    public UpdateCourseView() {
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        H2 heading = new H2("✏️ Update Paid Course");
        priceField.setStep(0.01);
        priceField.setPlaceholder("e.g. 499.00");

        updateButton.getStyle().set("background-color", "#28a745").set("color", "white");

        add(heading, nameField, descField, priceField, updateButton);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long courseIdParam) {
        if (courseIdParam == null) {
            add(new Paragraph("❌ No course ID provided."));
            updateButton.setEnabled(false);
            return;
        }

        this.courseId = courseIdParam;

        // Get user from localStorage to verify instructor
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
                                instructorId = ((Number) idObj).longValue();
                                fetchCourseData(courseId);
                            } else {
                                add(new Paragraph("❌ You must be an INSTRUCTOR to update a course."));
                                updateButton.setEnabled(false);
                            }

                        } else {
                            add(new Paragraph("❌ Invalid user data."));
                            updateButton.setEnabled(false);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        add(new Paragraph("❌ Error reading user data."));
                        updateButton.setEnabled(false);
                    }
                });
    }

    private void fetchCourseData(Long courseId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:8000/paid-courses/" + courseId, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> course = response.getBody();
                nameField.setValue((String) course.get("title"));
                descField.setValue((String) course.get("description"));
                priceField.setValue(((Number) course.get("price")).doubleValue());

                setupUpdateHandler();
            } else {
                Notification.show("❌ Failed to fetch course data.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("❌ Error fetching course.");
        }
    }

    private void setupUpdateHandler() {
        updateButton.addClickListener(event -> {
            String title = nameField.getValue().trim();
            String description = descField.getValue().trim();
            Double price = priceField.getValue();

            if (title.isEmpty() || description.isEmpty() || price == null) {
                Notification.show("⚠️ Please fill in all fields.");
                return;
            }

            Map<String, Object> instructor = new HashMap<>();
            instructor.put("id", instructorId);

            Map<String, Object> course = new HashMap<>();
            course.put("id", courseId);
            course.put("title", title);
            course.put("description", description);
            course.put("price", price);
            course.put("instructor", instructor);

            try {
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(course);
                restTemplate.postForEntity(
                    "http://localhost:8000/paid-courses/update/" + courseId, // ✅ Fix is here
                    request,
                    Void.class
                );
                Notification.show("✅ Course updated successfully.");
                UI.getCurrent().navigate("instructor-dashboard");
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("❌ Error updating course.");
            }
        });
    }
}
