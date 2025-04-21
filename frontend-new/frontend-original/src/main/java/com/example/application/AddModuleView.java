package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Route("add-module")
public class AddModuleView extends VerticalLayout implements BeforeEnterObserver {

    private final RestTemplate restTemplate = new RestTemplate();
    private Integer courseId;

    private TextField titleField;
    private TextField videoUrlField;
    private Button addButton;
    private Button updatePageButton;
    private Button dashboardButton;

    public AddModuleView() {
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        titleField = new TextField("Module Title");
        videoUrlField = new TextField("Video URL");

        addButton = new Button("Add Module");
        addButton.getStyle().set("background-color", "#28a745").set("color", "white");

        updatePageButton = new Button("Go to Update Page");
        dashboardButton = new Button("🏠 Back to Instructor Dashboard");
        dashboardButton.getStyle().set("background-color", "#007bff").set("color", "white");

        add(new H2("📚 Add Module"), titleField, videoUrlField, addButton, updatePageButton, dashboardButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            String idParam = event.getLocation().getQueryParameters().getParameters().get("courseId").get(0);
            courseId = Integer.parseInt(idParam);
        } catch (Exception e) {
            Notification.show("❌ Missing or invalid courseId in URL");
            return;
        }

        setupListeners();
    }

    private void setupListeners() {
        addButton.addClickListener(e -> {
            String title = titleField.getValue();
            String videoUrl = videoUrlField.getValue();

            if (title.isEmpty() || videoUrl.isEmpty()) {
                Notification.show("⚠️ Title and Video URL must not be empty");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("videoUrl", videoUrl);

            try {
                String endpoint = "http://localhost:8000/paid-course-modules/add/" + courseId;
                ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, payload, Void.class);
                Notification.show("✅ Module added!");
                titleField.clear();
                videoUrlField.clear();
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("❌ Error adding module");
            }
        });

        updatePageButton.addClickListener(e -> UI.getCurrent().navigate("update-course?courseId=" + courseId));
        dashboardButton.addClickListener(e -> UI.getCurrent().navigate("instructor-dashboard"));
    }
}
