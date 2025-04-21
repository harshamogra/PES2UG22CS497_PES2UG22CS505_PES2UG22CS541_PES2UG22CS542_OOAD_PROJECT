package com.example.application.controller;

import com.example.application.model.User;
import com.example.application.service.InMemoryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private InMemoryDataService inMemoryDataService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        List<User> users = inMemoryDataService.getAllUsers();
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
            }
        }

        inMemoryDataService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        List<User> users = inMemoryDataService.getAllUsers();
        for (User u : users) {
            if (u.getUsername().equals(user.getUsername()) &&
                u.getPassword().equals(user.getPassword()) &&
                u.getRole().equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.ok("Login successful");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
}
