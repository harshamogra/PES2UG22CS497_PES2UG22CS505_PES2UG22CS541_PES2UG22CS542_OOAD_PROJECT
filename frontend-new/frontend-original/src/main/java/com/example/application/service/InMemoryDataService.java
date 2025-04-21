package com.example.application.service;

import com.example.application.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InMemoryDataService {

    // List to store users in memory
    private List<User> users = new ArrayList<>();

    // Method to add a user
    public void addUser(User user) {
        users.add(user);
    }

    // Method to get all users
    public List<User> getAllUsers() {
        return users;
    }

    // Method to clear all users
    public void clearUsers() {
        users.clear();
    }
}
