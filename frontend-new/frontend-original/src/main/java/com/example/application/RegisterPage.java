package com.example.application;

import com.example.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;

@Route("register")
@PageTitle("Register")
public class RegisterPage extends Div {

    private final UserService userService;

    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Select<String> roleSelect;
    private Button registerButton;

    @Autowired
    public RegisterPage(UserService userService) {
        this.userService = userService;

        setSizeFull();

        Div titleWrapper = new Div();
        titleWrapper.getStyle()
            .set("position", "absolute")
            .set("top", "40px")
            .set("text-align", "center")
            .set("width", "100%");

        H2 mainHeading = new H2("📘 E-Learning Platform");
        mainHeading.getStyle()
            .set("color", "#1f2937")
            .set("font-size", "28px")
            .set("margin", "0");

        titleWrapper.add(mainHeading);
        add(titleWrapper);

        getStyle().set("display", "flex")
                  .set("justify-content", "center")
                  .set("align-items", "center")
                  .set("background", "#f3f4f6");

        H2 heading = new H2("Create Your Account");
        heading.getStyle()
            .set("margin-bottom", "20px")
            .set("margin-top", "100px")
            .set("color", "#0077cc")
            .set("font-weight", "600");

        FormLayout formLayout = new FormLayout();
        formLayout.getStyle()
            .set("padding", "30px")
            .set("border-radius", "12px")
            .set("box-shadow", "0 8px 20px rgba(0, 0, 0, 0.1)")
            .set("background-color", "#ffffff")
            .set("width", "380px");

        usernameField = new TextField("Username");
        passwordField = new PasswordField("Password");
        confirmPasswordField = new PasswordField("Confirm Password");

        // 👇 Add the role dropdown
        roleSelect = new Select<>();
        roleSelect.setLabel("Register As");
        roleSelect.setItems("STUDENT", "INSTRUCTOR");
        roleSelect.setValue("STUDENT"); // default value

        registerButton = new Button("Register", e -> register());
        registerButton.getStyle()
            .set("margin-top", "10px")
            .set("width", "100%")
            .set("background-color", "#0077cc")
            .set("color", "white")
            .set("border", "none")
            .set("border-radius", "8px")
            .set("padding", "10px 0");

        formLayout.add(heading, usernameField, passwordField, confirmPasswordField, roleSelect, registerButton);

        add(formLayout);
    }

    private void register() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        String role = roleSelect.getValue();

        if (!password.equals(confirmPassword)) {
            Notification.show("Passwords do not match!", 3000, Notification.Position.MIDDLE);
            return;
        }

        boolean success = userService.registerUser(username, password, role);

        if (success) {
            Notification.show("User registered successfully!", 3000, Notification.Position.MIDDLE);
            getUI().ifPresent(ui -> ui.navigate("login"));
        } else {
            Notification.show("Registration failed! Username might be taken.", 3000, Notification.Position.MIDDLE);
        }
    }
}
