package project.OOAD_PROJECT.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import project.OOAD_PROJECT.model.User;
import project.OOAD_PROJECT.service.UserService;
import project.OOAD_PROJECT.model.AuthResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        AuthResponse response = userService.register(user);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getMessage());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response.getMessage()); // 409 if username is taken
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        AuthResponse response = userService.login(user);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response); // ✅ Returns message + user object
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response.getMessage());
        }
    }

}
