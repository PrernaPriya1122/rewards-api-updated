# Rewards API - Spring Boot Assignment

## Project Overview
This project is a Spring Boot REST API application developed for calculating customer reward points based on purchase transactions.

## Reward Rules
- 2 points for every dollar spent above $100
- 1 point for every dollar spent between $50 and $100
- No points for amounts below $50

Example:
- Purchase = $120
- Points = (20 × 2) + 50 = 90

# Technologies Used
- Java 17
- Spring Boot 3
- Maven
- JUnit 5
- MockMvc

# Project Structure

rewards-api
├── controller
├── service
├── model
├── dto
├── exception
├── test
├── pom.xml
└── README.md


# API Endpoint

GET /api/rewards


# Implementation Details

## Reward Calculation

### Amount > 100
((amount - 100) * 2) + 50

### Amount between 50 and 100
amount - 50

### Amount below 50
0 points


# Testing

## Unit Tests
- Amount > 100
- Amount between 50 and 100
- Amount < 50
- Negative amount exception

## Integration Tests
- Endpoint testing using MockMvc


# Exception Handling

Implemented using:
@RestControllerAdvice

Handles:
- IllegalArgumentException


# Run Application

mvn clean install
mvn spring-boot:run



# API URL

http://localhost:8080/api/rewards

