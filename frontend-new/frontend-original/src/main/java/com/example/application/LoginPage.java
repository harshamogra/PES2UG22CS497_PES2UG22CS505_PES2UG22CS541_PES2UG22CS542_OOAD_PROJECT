package com.example.application;

import com.example.application.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Route("login")
public class LoginPage extends Div {

    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleDropdown;
    private Button loginButton;

    @Autowired
    public LoginPage(UserService userService) {
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

        H2 heading = new H2("Login to Your Account");
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

        roleDropdown = new ComboBox<>("Select Role");
        roleDropdown.setItems("STUDENT", "INSTRUCTOR");
        roleDropdown.setPlaceholder("Choose role");

        loginButton = new Button("Login", e -> login());
        loginButton.getStyle()
                .set("margin-top", "10px")
                .set("width", "100%")
                .set("background-color", "#0077cc")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "8px")
                .set("padding", "10px 0");

        Anchor registerLink = new Anchor("/register", "Don't have an account? Register here");
        Div linkContainer = new Div(registerLink);
        linkContainer.getStyle().set("margin-top", "10px");

        formLayout.add(heading, usernameField, passwordField, roleDropdown, loginButton, linkContainer);
        add(formLayout);
    }

    private void login() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        String role = roleDropdown.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            Notification.show("Please fill all fields and select a role.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Map<String, Object> user = userService.loginUser(username, password, role);

        if (user != null) {
            Notification.show("Login successful!", 2000, Notification.Position.MIDDLE);

            // ✅ Store user in VaadinSession for backend access
            VaadinSession.getCurrent().setAttribute("user", user);

            try {
                String userJson = objectMapper.writeValueAsString(user);
                UI.getCurrent().getPage().executeJs("""
                    localStorage.setItem('user', $0);
                    const user = JSON.parse(localStorage.getItem('user'));
                    if (user.role === 'INSTRUCTOR') {
                        window.location.href = '/instructor-dashboard';
                    } else {
                        window.location.href = '/student-dashboard';
                    }
                """, userJson);
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Failed to parse user data.", 3000, Notification.Position.MIDDLE);
            }
            

        } else {
            Notification.show("Invalid credentials!", 3000, Notification.Position.MIDDLE);
        }
    }
}
