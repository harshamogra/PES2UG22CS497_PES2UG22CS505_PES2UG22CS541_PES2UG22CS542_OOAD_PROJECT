package com.example.application;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.vaadin.flow.component.notification.Notification;


import java.util.List;
import java.util.Map;

@Route("home")
public class HomePage extends VerticalLayout {

    private String username = "User"; // TODO: Replace with actual logged-in username

    

    public HomePage() {
        setSpacing(true);
        setPadding(true);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("background-color", "#f5f5f5");

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
        .set("padding", "8px 16px")
        .set("cursor", "pointer");
        addCourseBtn.addClickListener(e -> UI.getCurrent().navigate("add-course"));

        add(addCourseBtn);


        H2 freeHeading = new H2("🎓 Free Courses");
        freeHeading.getStyle().set("margin-top", "20px").set("color", "#0077cc");

        H2 paidHeading = new H2("💰 Paid Courses (Coming Soon)");
        paidHeading.getStyle().set("margin-top", "50px").set("color", "#999");

        // Free Courses section
        Div freeCoursesContainer = new Div();
        freeCoursesContainer.getStyle()
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("justify-content", "center")
            .set("gap", "20px")
            .set("margin-top", "20px");

        loadCourses(freeCoursesContainer);

        add(freeHeading, freeCoursesContainer, paidHeading);
    }

    private void loadCourses(Div container) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8000/free-courses/all";

        try {
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> courses = response.getBody();

            if (courses != null) {
                for (Map<String, Object> course : courses) {
                    Div card = new Div();
                    card.getStyle()
                        .set("background-color", "#fff")
                        .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                        .set("padding", "20px")
                        .set("border-radius", "12px")
                        .set("width", "350px")
                        .set("text-align", "center");

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
                                            String deleteUrl = "http://localhost:8000/free-courses/delete/" + courseId;
                                            restTemplate.delete(deleteUrl); // use the same instance declared earlier
                                            Notification.show("✅ Course deleted!");
                                            confirmDialog.close();
                                            container.remove(card);
                                            //UI.getCurrent().getPage().reload(); // Refresh the page
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
                        videoFrame.getElement().setAttribute("allow", "fullscreen"); // Explicitly allow fullscreen
                        videoFrame.getStyle().set("border", "none").set("margin-top", "10px");
                        card.add(videoFrame);
                    } else {
                        Anchor openLink = new Anchor(link, "Open Course Link");
                        openLink.setTarget("_blank");
                        card.add(openLink);
                    }

                    container.add(card);
                    card.add(deleteBtn);

                }
            }
        } catch (Exception e) {
            container.add(new Paragraph("❌ Failed to load courses."));
            e.printStackTrace();
        }
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
            if (link.contains("/view")) {
                try {
                    String fileId = link.split("/d/")[1].split("/")[0];
                    return "https://drive.google.com/file/d/" + fileId + "/preview";
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
}
