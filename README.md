# Student Management System

This project is a E-learning Platform built using Java, implementing Object-Oriented Analysis and Design (OOAD) principles. The system consists of a Vaadin-based frontend application and a Spring Boot backend server with PostgreSQL database.

## Prerequisites

- Java JDK 11 or higher
- Maven 3.6 or higher
- Git
- PostgreSQL database

## Setup and Running the Project

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd Backend-new
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

The backend server will start on `http://localhost:8000`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend-new/frontend-original
   ```

2. Compile the Java files:
   ```bash
   javac *.java
   ```

3. Run the application:
   ```bash
   java StudentDashboard
   ```

The frontend application will be available at `http://localhost:3000`

## Features

- Student Dashboard for viewing and managing student information
- Buy Page for handling student purchases
- RESTful API endpoints for data management
- Object-Oriented design patterns and principles

## Project Architecture

The project follows a client-server architecture:
- Frontend: Vaadin-based web application running on port 3000
- Backend: Spring Boot REST API running on port 8000
- Database: PostgreSQL for data persistence

