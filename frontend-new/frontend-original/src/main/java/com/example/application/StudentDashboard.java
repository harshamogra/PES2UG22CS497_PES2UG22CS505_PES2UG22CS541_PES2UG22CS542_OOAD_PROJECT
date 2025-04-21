package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.vaadin.flow.component.notification.Notification;

import java.util.List;
import java.util.Map;

@Route("student-dashboard")
public class StudentDashboard extends VerticalLayout {

    private String username;
    private Long userId;
    private List<Integer> purchasedCourseIds;

    private final RestTemplate restTemplate = new RestTemplate();

    public StudentDashboard() {
        setSpacing(true);
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        // Get session user data
        Map<String, Object> userMap = (Map<String, Object>) VaadinSession.getCurrent().getAttribute("user");

        if (userMap == null) {
            Notification.show("You must log in first.");
            UI.getCurrent().navigate("login");
            return;
        }

        this.username = (String) userMap.get("username");
        this.userId = ((Number) userMap.get("id")).longValue();

        H1 welcomeMsg = new H1("👋 Welcome back, " + username + "!");
        welcomeMsg.getStyle().set("margin-top", "30px").set("color", "#333");
        add(welcomeMsg);

        Button addCourseBtn = new Button("➕ Add Course");
        addCourseBtn.getStyle()
            .set("margin-top", "10px")
            .set("background-color", "#0077cc")
            .set("color", "white")
            .set("border", "none")
            .set("border-radius", "8px")
            .set("padding", "8px 16px");
        addCourseBtn.addClickListener(e -> UI.getCurrent().navigate("add-course"));
        add(addCourseBtn);

        // Free Courses Section
        H2 freeHeading = new H2("🎓 Free Courses");
        freeHeading.getStyle().set("margin-top", "20px").set("color", "#0077cc");

        Div freeCoursesContainer = new Div();
        freeCoursesContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("justify-content", "center")
            .set("gap", "20px");
        loadCourses("http://localhost:8000/free-courses/all", freeCoursesContainer, false);

        // Paid Courses Section
        H2 paidHeading = new H2("💰 Your Paid Courses");
        paidHeading.getStyle().set("margin-top", "50px").set("color", "#0077cc");

        Div paidCoursesContainer = new Div();
        paidCoursesContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("justify-content", "center")
            .set("gap", "20px");

        // Load purchased courses directly from backend API
        try {
            String purchasedCoursesUrl = "http://localhost:8000/paid-courses/purchased/" + userId;
            ResponseEntity<List> response = restTemplate.getForEntity(purchasedCoursesUrl, List.class);
            List<Map<String, Object>> purchasedCourses = response.getBody();
        
            if (purchasedCourses != null && !purchasedCourses.isEmpty()) {
                for (Map<String, Object> course : purchasedCourses) {
                    Div card = new Div();
                    card.getStyle()
                        .set("background-color", "#fff")
                        .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                        .set("padding", "20px")
                        .set("border-radius", "12px")
                        .set("width", "1400px")
                        .set("text-align", "center");
        
                    String courseName = (String) course.get("title");
                    String description = (String) course.get("description");
        
                    Map<String, Object> instructor = (Map<String, Object>) course.get("instructor");
                    String instructorName = instructor != null ? (String) instructor.get("username") : "Unknown";
        
                    card.add(new H3(courseName));
                    card.add(new Paragraph("👨‍🏫 Instructor: " + instructorName));
                    card.add(new Paragraph(description));
        
                    List<Map<String, Object>> modules = (List<Map<String, Object>>) course.get("modules");
                    if (modules != null && !modules.isEmpty()) {
                        for (Map<String, Object> module : modules) {
                            String moduleTitle = (String) module.get("title");
                            String videoUrl = (String) module.get("videoUrl");
        
                            card.add(new H4("📦 Module: " + moduleTitle));
        
                            String embedUrl = getEmbedUrl(videoUrl);
                            if (embedUrl != null) {
                                IFrame videoFrame = new IFrame(embedUrl);
                                videoFrame.setWidth("1000px");
                                videoFrame.setHeight("600px");
                                videoFrame.getElement().setAttribute("allowfullscreen", "true");
                                videoFrame.getStyle().set("border", "none").set("margin-top", "5px");
                                card.add(videoFrame);
                            } else if (videoUrl != null) {
                                Anchor openLink = new Anchor(videoUrl, "📎 Open Video");
                                openLink.setTarget("_blank");
                                card.add(openLink);
                            }
                        }
                    } else {
                        card.add(new Paragraph("📭 No modules available."));
                    }
        
                    paidCoursesContainer.add(card);
                }
            } else {
                paidCoursesContainer.add(new Paragraph("❌ You haven't purchased any courses yet."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            paidCoursesContainer.add(new Paragraph("❌ Failed to load purchased courses."));
        }
        

        Button buyCoursesBtn = new Button("💳 Buy Courses");
        buyCoursesBtn.getStyle()
            .set("margin-top", "10px")
            .set("background-color", "#28a745")
            .set("color", "white")
            .set("border", "none")
            .set("border-radius", "8px")
            .set("padding", "8px 16px");

        buyCoursesBtn.addClickListener(e -> UI.getCurrent().navigate("buy-courses"));

        add(buyCoursesBtn);


        add(freeHeading, freeCoursesContainer, paidHeading, paidCoursesContainer);
    }

    private void loadCourses(String url, Div container, boolean hideDelete) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> courses = response.getBody();

            if (courses != null) {
                for (Map<String, Object> course : courses) {
                    container.add(createCourseCard(course, container, hideDelete));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            container.add(new Paragraph("❌ Failed to load courses."));
        }
    }

    private Div createCourseCard(Map<String, Object> course, Div container, boolean hideDelete) {
        Div card = new Div();
        card.getStyle()
            .set("background-color", "#fff")
            .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
            .set("padding", "20px")
            .set("border-radius", "12px")
            .set("width", "350px")
            .set("text-align", "center");

        String name = (String) course.get("courseName");
        String desc = (String) course.get("description");
        String link = (String) course.get("link");

        H3 courseTitle = new H3(name);
        Paragraph description = new Paragraph(desc);
        description.getStyle().set("color", "#555");

        card.add(courseTitle, description);

        String embedUrl = getEmbedUrl(link);
        if (embedUrl != null) {
            IFrame videoFrame = new IFrame(embedUrl);
            videoFrame.setWidth("320px");
            videoFrame.setHeight("180px");
            videoFrame.getElement().setAttribute("allowfullscreen", "true");
            videoFrame.getElement().setAttribute("allow", "fullscreen");
            videoFrame.getStyle().set("border", "none").set("margin-top", "10px");
            card.add(videoFrame);
        } else {
            Anchor openLink = new Anchor(link, "Open Course Link");
            openLink.setTarget("_blank");
            card.add(openLink);
        }

        if (!hideDelete) {
            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.getStyle()
                .set("background-color", "#ff4d4d")
                .set("color", "white")
                .set("margin-top", "10px");

            deleteBtn.addClickListener(event -> {
                Long courseId = ((Number) course.get("id")).longValue();
                Dialog confirmDialog = new Dialog();
                confirmDialog.setHeaderTitle("Confirm Delete");

                VerticalLayout dialogLayout = new VerticalLayout();
                dialogLayout.add(new Paragraph("Are you sure you want to delete this course?"));

                Button confirmButton = new Button("Yes, Delete", e -> {
                    try {
                        restTemplate.delete("http://localhost:8000/free-courses/delete/" + courseId);
                        Notification.show("✅ Course deleted!");
                        confirmDialog.close();
                        container.remove(card);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Notification.show("❌ Failed to delete course.");
                    }
                });

                Button cancelButton = new Button("Cancel", e -> confirmDialog.close());
                confirmButton.getStyle().set("background-color", "#ff4d4d").set("color", "white");
                cancelButton.getStyle().set("margin-left", "10px");

                HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
                dialogLayout.add(buttons);

                confirmDialog.add(dialogLayout);
                confirmDialog.open();
            });

            card.add(deleteBtn);
        }

        return card;
    }

    private String getEmbedUrl(String link) {
        if (link.contains("youtu.be")) {
            String videoId = link.substring(link.lastIndexOf("/") + 1);
            return "https://www.youtube.com/embed/" + videoId;
        } else if (link.contains("youtube.com/watch?v=")) {
            String videoId = link.substring(link.indexOf("v=") + 2);
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return "https://www.youtube.com/embed/" + videoId;
        } else if (link.contains("drive.google.com")) {
            try {
                String fileId = link.split("/d/")[1].split("/")[0];
                return "https://drive.google.com/file/d/" + fileId + "/preview";
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
