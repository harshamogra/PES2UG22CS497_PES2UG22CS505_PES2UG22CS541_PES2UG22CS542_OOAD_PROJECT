package project.OOAD_PROJECT.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.OOAD_PROJECT.model.User;
import project.OOAD_PROJECT.repository.UserRepository;
import project.OOAD_PROJECT.model.AuthResponse;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register a new user
    public AuthResponse register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return new AuthResponse("Username already taken", false);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        userRepository.save(user);
        return new AuthResponse("User registered successfully", true);
    }

    // Login user
    public AuthResponse login(User user) {
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
    
        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
    
            if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
                return new AuthResponse("Login successful", true, foundUser);
            }
        }
    
        return new AuthResponse("Invalid username or password", false);
    }
    
    
    
    
}
