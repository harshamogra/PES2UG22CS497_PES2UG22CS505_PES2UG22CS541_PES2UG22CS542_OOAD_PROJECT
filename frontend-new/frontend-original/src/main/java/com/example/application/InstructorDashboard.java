package com.example.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import elemental.json.JsonValue;
import elemental.json.JsonType;

@Route("instructor-dashboard")
public class InstructorDashboard extends VerticalLayout {

    private final RestTemplate restTemplate = new RestTemplate();

    public InstructorDashboard() {
        setWidthFull();
        setSpacing(false);
        setPadding(false);

        H1 mainHeading = new H1("📋 Instructor Dashboard");
        mainHeading.getStyle()
        .set("margin", "20px")
        .set("color", "#333333");

        add(mainHeading); 

        // Add feedback button here
        // In the constructor, after the welcomeMessage is created
        Button feedbackButton = new Button("📋 Manage Student Feedback", e -> UI.getCurrent().navigate("instructor-feedback"));
        feedbackButton.getStyle()
            .set("background-color", "#6610f2")
            .set("color", "white")
            .set("margin", "10px 0")
            .set("padding", "10px");
        add(feedbackButton);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Div profileCard = new Div();
        profileCard.setWidth("300px");
        profileCard.setHeight("100px");
        profileCard.getStyle()
        .set("border", "1px solid #ccc")
        .set("padding", "10px")
        .set("border-radius", "10px")
        .set("background-color", "#ADD8E6")
        .set("box-shadow", "2px 2px 6px rgba(0,0,0,0.1)");

        Span dashboardLabel = new Span("📋 Instructor Dashboard");
        Span dashboardLabel2 = new Span("Welcome ");
        dashboardLabel.getStyle().set("font-weight", "bold");
        dashboardLabel2.getStyle().set("font-weight", "bold");

        Span usernameLabel = new Span(); // will set it after reading from storage

        Button logoutButton = new Button("🚪 Logout");
        logoutButton.getStyle().set("margin-top", "5px").set("border", "2px solid #FF0000").set("background-color", "#FF0000").set("color", "#ffffff");
        logoutButton.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs("localStorage.removeItem('user'); window.location.href='login'");
        });

        VerticalLayout cardContent = new VerticalLayout(dashboardLabel, usernameLabel, logoutButton);
        cardContent.setSpacing(false);
        cardContent.setPadding(false);
        profileCard.add(cardContent);
        header.add(profileCard);
        add(header);


        Button addCourseBtn = new Button("➕ Add Course");
        addCourseBtn.addClickListener(e -> UI.getCurrent().navigate("add-paid-course"));
        add(addCourseBtn);

        Div welcomeMessage = new Div();
        welcomeMessage.setWidthFull();

        Div courseContainer = new Div();
        courseContainer.setWidthFull();

        add(welcomeMessage, courseContainer);

        UI.getCurrent().getPage().executeJs("""
            if (!localStorage.getItem('user')) {
                localStorage.setItem('user', JSON.stringify({
                    id: 3,
                    username: 'teacher_testing',
                    role: 'INSTRUCTOR'
                }));
            }
        """);

        UI.getCurrent().getPage().executeJs("return window.localStorage.getItem('user');")
            .then(userJson -> {
                try {
                    if (userJson != null && userJson instanceof JsonValue jsonValue &&
                        jsonValue.getType() == JsonType.STRING &&
                        jsonValue.asString() != null && !jsonValue.asString().isEmpty()) {

                        String jsonString = jsonValue.asString();

                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> userMap = mapper.readValue(jsonString, Map.class);

                        Object idObj = userMap.get("id");
                        Object roleObj = userMap.get("role");
                        Object usernameObj = userMap.get("username");

                        if (idObj instanceof Number && "INSTRUCTOR".equals(String.valueOf(roleObj))) {
                            Long instructorId = ((Number) idObj).longValue();
                            welcomeMessage.add(new Paragraph("👋 Welcome, " + usernameObj));
                            usernameLabel.setText("👋 Welcome " + usernameObj);
                            loadInstructorCourses(courseContainer, instructorId);
                        } else {
                            courseContainer.add(new Paragraph("❌ You must be logged in as an INSTRUCTOR."));
                        }
                    } else {
                        courseContainer.add(new Paragraph("❌ User not found in local storage."));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    courseContainer.add(new Paragraph("❌ Error parsing user from local storage."));
                }
            });
    }

    private void loadInstructorCourses(Div container, Long instructorId) {
        container.removeAll();
        try {
            String url = "http://localhost:8000/paid-courses/instructor/" + instructorId;
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> courses = response.getBody();

            if (courses == null || courses.isEmpty()) {
                container.add(new Paragraph("❌ You haven't created any paid courses yet."));
            } else {
                for (Map<String, Object> course : courses) {
                    container.add(createCourseCard(course, container, instructorId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            container.add(new Paragraph("❌ Failed to load instructor courses."));
        }
    }

    private Div createCourseCard(Map<String, Object> course, Div container, Long instructorId) {
        Div card = new Div();
        card.setWidthFull(); // Use full width
        card.getStyle()
            .set("margin", "10px 0")
            .set("border", "1px solid #ccc")
            .set("padding", "10px")
            .set("border-radius", "8px")
            .set("background-color", "#f9f9f9");

        Long courseId = ((Number) course.get("id")).longValue();
        String title = (String) course.get("title");
        String description = (String) course.get("description");
        Double price = (Double) course.get("price");

        Map<String, Object> instructor = (Map<String, Object>) course.get("instructor");
        String instructorName = instructor != null ? (String) instructor.get("username") : "Unknown";

        card.add(new H3("📘 " + title));
        card.add(new Paragraph("💬 " + (description != null ? description : "No description")));
        card.add(new Paragraph("💵 Price: ₹" + price));
        card.add(new Paragraph("👨‍🏫 Instructor: " + instructorName));

        List<Map<String, Object>> modules = (List<Map<String, Object>>) course.get("modules");
        if (modules != null && !modules.isEmpty()) {
            card.add(new H4("📚 Modules:"));
        
            for (Map<String, Object> module : modules) {
                String moduleTitle = (String) module.get("title");
                String videoUrl = (String) module.get("videoUrl");
        
                String fileId = extractDriveFileId(videoUrl);
                if (fileId != null) {
                    String embedUrl = "https://drive.google.com/file/d/" + fileId + "/preview";
        
                    Long moduleId = ((Number) module.get("id")).longValue(); // extract module ID

                Div moduleSection = new Div();
                moduleSection.setWidthFull();
                moduleSection.getStyle().set("margin-top", "10px");

                H5 moduleTitleElement = new H5("▶️ " + moduleTitle);
                Button showVideoButton = new Button("🎬 Show Video");

                IFrame videoFrame = new IFrame(embedUrl);
                videoFrame.setWidth("100%");
                videoFrame.setHeight("600px");
                videoFrame.getStyle().set("border", "none");
                videoFrame.setVisible(false);

                showVideoButton.addClickListener(e -> {
                    boolean visible = videoFrame.isVisible();
                    videoFrame.setVisible(!visible);
                    showVideoButton.setText(visible ? "🎬 Show Video" : "❌ Hide Video");
                });

                // Buttons for update and delete
                Button deleteModuleBtn = new Button("🗑️ Delete Module");
                deleteModuleBtn.getStyle().set("background-color", "#c0392b").set("color", "white");
                deleteModuleBtn.addClickListener(evt -> {
                    try {
                        restTemplate.delete("http://localhost:8000/paid-course-modules/delete/" + moduleId);
                        Notification.show("✅ Module deleted");
                        loadInstructorCourses(container, instructorId); // reload to reflect changes
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Notification.show("❌ Failed to delete module");
                    }
                });

                Button updateModuleBtn = new Button("✏️ Update Module");
                updateModuleBtn.getStyle().set("background-color", "#27ae60").set("color", "white");
                updateModuleBtn.addClickListener(evt -> {
                    UI.getCurrent().navigate("update-module?moduleId=" + moduleId + "&courseId=" + courseId);
                });

                HorizontalLayout moduleBtnRow = new HorizontalLayout(showVideoButton, updateModuleBtn, deleteModuleBtn);
                moduleBtnRow.setSpacing(true);
                moduleBtnRow.setPadding(true);

                moduleSection.add(moduleTitleElement, moduleBtnRow, videoFrame);
                card.add(moduleSection);

                        
                } else {
                    card.add(new Paragraph("❌ Invalid video link for module: " + moduleTitle));
                }
            }
        }
        
        
        

        Div buttons = new Div();
        buttons.getStyle().set("margin-top", "10px")
                .set("display", "flex")
                .set("gap", "10px");

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.getStyle().set("background-color", "#e74c3c").set("color", "white");
        deleteBtn.addClickListener(e -> {
            try {
                restTemplate.delete("http://localhost:8000/paid-courses/delete/" + courseId);
                Notification.show("✅ Course deleted");
                loadInstructorCourses(container, instructorId);
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("❌ Failed to delete course");
            }
        });

        Button editBtn = new Button("✏️ Edit");
        editBtn.getStyle().set("background-color", "#f39c12").set("color", "white");
        editBtn.addClickListener(e -> UI.getCurrent().navigate("update-course/" + courseId));

        Button addModuleBtn = new Button("➕ Add Module");
        addModuleBtn.getStyle().set("background-color", "#2980b9").set("color", "white");
        addModuleBtn.addClickListener(e -> UI.getCurrent().navigate("add-module?courseId=" + courseId));

        Button createAssessmentBtn = new Button("📝 Create Assessment");
        createAssessmentBtn.getStyle().set("background-color", "#8e44ad").set("color", "white");
        createAssessmentBtn.addClickListener(e -> UI.getCurrent().navigate("create-assessment?courseId=" + courseId));

        // In the createCourseCard method, add a button to view course reviews
        // Add this to the buttons div, after the other buttons
        
        Button viewReviewsBtn = new Button("⭐ View Reviews");
        viewReviewsBtn.getStyle().set("background-color", "#f39c12").set("color", "white");
        viewReviewsBtn.addClickListener(e -> UI.getCurrent().navigate("course-reviews?courseId=" + courseId));
        
        buttons.add(viewReviewsBtn);
        buttons.add(editBtn, deleteBtn, addModuleBtn, createAssessmentBtn);
        card.add(buttons);

        return card;
    }

    private String extractDriveFileId(String url) {
        try {
            if (url != null && url.contains("/file/d/")) {
                int start = url.indexOf("/file/d/") + 8;
                int end = url.indexOf("/", start + 1);
                if (end == -1) end = url.indexOf("?", start + 1);
                if (end == -1) end = url.length();
                return url.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}  // Add this closing brace for the class
