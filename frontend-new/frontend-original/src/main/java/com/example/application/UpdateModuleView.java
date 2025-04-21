package com.example.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Route("update-module")
public class UpdateModuleView extends VerticalLayout implements BeforeEnterObserver {

    private final RestTemplate restTemplate = new RestTemplate();

    private TextField titleField = new TextField("Module Title");
    private TextField videoUrlField = new TextField("Video URL");

    private Button updateButton = new Button("💾 Update Module");
    private Button backButton = new Button("🏠 Back to Dashboard");

    private Long moduleId;
    private Long courseId;

    public UpdateModuleView() {
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        H2 heading = new H2("✏️ Update Module");

        updateButton.getStyle().set("background-color", "#28a745").set("color", "white");
        backButton.getStyle().set("background-color", "#007bff").set("color", "white");

        add(heading, titleField, videoUrlField, updateButton, backButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            String moduleParam = event.getLocation().getQueryParameters().getParameters().get("moduleId").get(0);
            String courseParam = event.getLocation().getQueryParameters().getParameters().get("courseId").get(0);
            moduleId = Long.parseLong(moduleParam);
            courseId = Long.parseLong(courseParam);
        } catch (Exception e) {
            add(new Paragraph("❌ Missing or invalid parameters."));
            updateButton.setEnabled(false);
            return;
        }

        fetchModuleData(courseId, moduleId);
        setupListeners();
    }

    private void fetchModuleData(Long courseId, Long moduleId) {
        try {
            String url = "http://localhost:8000/paid-course-modules/" + courseId + "/" + moduleId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
    
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> module = response.getBody();
                titleField.setValue((String) module.get("title"));
                videoUrlField.setValue((String) module.get("videoUrl"));
            } else {
                Notification.show("❌ Failed to fetch module data.");
                updateButton.setEnabled(false);
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("❌ Error fetching module.");
            updateButton.setEnabled(false);
        }
    }
    
    private void setupListeners() {
        updateButton.addClickListener(event -> {
            String title = titleField.getValue().trim();
            String videoUrl = videoUrlField.getValue().trim();

            if (title.isEmpty() || videoUrl.isEmpty()) {
                Notification.show("⚠️ Please fill in all fields.");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("id", moduleId);
            payload.put("title", title);
            payload.put("videoUrl", videoUrl);

            try {
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload);
                restTemplate.put(
                    "http://localhost:8000/paid-course-modules/update/" + moduleId,
                    request
                );
                Notification.show("✅ Module updated successfully.");
                UI.getCurrent().navigate("instructor-dashboard");
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("❌ Error updating module.");
            }
            
        });

        backButton.addClickListener(e -> UI.getCurrent().navigate("instructor-dashboard"));
    }
}
