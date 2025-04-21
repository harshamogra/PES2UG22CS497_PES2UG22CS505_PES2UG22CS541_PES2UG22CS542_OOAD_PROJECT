package com.example.application;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Route("add-course")
public class AddFreeCoursePage extends VerticalLayout {

    public AddFreeCoursePage() {
        setAlignItems(Alignment.CENTER);
        setWidthFull();

        H2 heading = new H2("➕ Add a Free Course");
        heading.getStyle().set("margin-bottom", "20px");

        TextField nameField = new TextField("Course Name");
        nameField.setWidth("300px");

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidth("300px");

        TextField linkField = new TextField("Course Link");
        linkField.setWidth("300px");

        Button submit = new Button("Add Course", event -> {
            Map<String, String> courseData = new HashMap<>();
            courseData.put("courseName", nameField.getValue());
            courseData.put("description", descriptionField.getValue());
            courseData.put("link", linkField.getValue());

            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<Map<String, String>> request = new HttpEntity<>(courseData);
                String response = restTemplate.postForObject("http://localhost:8000/free-courses/add", request, String.class);
                Notification.show(response);
                nameField.clear();
                descriptionField.clear();
                linkField.clear();
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("❌ Failed to add course");
            }
        });

        submit.getStyle()
              .set("margin-top", "10px")
              .set("background-color", "#6200ee")
              .set("color", "white");

        add(heading, nameField, descriptionField, linkField, submit);
    }
}
