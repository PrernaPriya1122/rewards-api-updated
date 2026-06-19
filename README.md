Retailer Program

The Retailer Rewards Program is a Spring Boot REST API application that calculates reward points earned by customers based on their purchase transactions.
The application retrieves customer transaction records from the database, calculates reward points for each transaction, aggregates rewards monthly,
calculates total reward points for each customer, and exposes the results through REST APIs.
The reward calculation period is configurable through application properties, allowing rewards to be calculated for a specified number of months without code changes.
The application also includes global exception handling, request validation, automated sample data loading using SQL scripts, and comprehensive unit and integration test coverage for positive, negative, and exception scenarios.

Business Requirement

A retailer wants to reward customers based on their spending.
2 points for every dollar spent above $100
1 point for every dollar spent between $50 and $100
No points for amounts below $50

Example:
Purchase = $120
Points = (20 × 2) + 50 = 90

Assumptions

Transaction amounts cannot be negative.
Customers may exist without any associated transactions.
A customer must exist in the system before reward details can be retrieved.
Reward summaries are returned at the customer level with both monthly and total rewards.
Date range searches return rewards only for transactions that fall within the specified start and end dates.
Sample customer and transaction data are loaded using SQL scripts located under the application resources directory.
The application uses an H2 database for storing customer and transaction data.
Validation and exception handling are applied to ensure meaningful API responses for invalid requests and error scenarios.

Technology Stack
┌───────────────────┬────────────────────┐
│ Technology        │ Version            │
├───────────────────┼────────────────────┤
│ Java              │ 17                 │
│ Spring Boot       │ 3.5.0              │
│ Spring Data JPA   │ Latest             │
│ Hibernate         │ Latest             │
│ H2 Database       │ In-Memory          │
│ Maven             │ Build Tool         │
│ Lombok            │ Latest             │
│ JUnit 5           │ Testing            │
│ Mockito           │ Mocking            │
│ MockMvc           │ Controller Testing │
└───────────────────┴────────────────────┘

Project Structure

src
├── main
│   ├── java
│   │   └── com.rewards.api
│   │       ├── controller
│   │       │   └── RewardController
│   │       ├── dto
│   │       │   ├── ErrorResponse
│   │       │   ├── MonthlyReward
│   │       │   └── RewardResponse
│   │       ├── exception
│   │       │   ├── CustomerNotFoundException
│   │       │   ├── TransactionNotFoundException
│   │       │   └── GlobalExceptionHandler
│   │       ├── model
│   │       │   ├── Customer
│   │       │   └── Transaction
│   │       ├── Repository
│   │       │   ├── CustomerRepository
│   │       │   └── TransactionRepository
│   │       ├── service
│   │       │   ├── RewardService
│   │       │   └── RewardServiceImpl
│   │       └── RewardsApiApplication
│   │
│   └── resources
│       ├── application.properties
│       └── data.sql
│
└── test
└── java
└── com.rewards.api
├── controller
│   ├── RewardControllerTest
│   └── RewardControllerIntegrationTest
└── service
└── RewardServiceImplTest

Data Model

┌──────────────────────────────────────────────┐
│                   Customer                   │
├─────────────────┬────────────────────────────┤
│ Field           │ Type                       │
├─────────────────┼────────────────────────────┤
│ customerId      │ Long                       │
│ customerName    │ String                     │
└─────────────────┴────────────────────────────┘
┌──────────────────────────────────────────────┐
│                  Transaction                 │
├─────────────────┬────────────────────────────┤
│ Field           │ Type                       │
├─────────────────┼────────────────────────────┤
│ id              │ Long                       │
│ customer        │ Customer                   │
│ amount          │ BigDecimal                 │
│ transactionDate │ LocalDate                  │
└─────────────────┴────────────────────────────┘
Relationship:

One Customer can have multiple Transactions.
One Transaction belongs to one Customer.

API Endpoints

1.Get Rewards For All Customers
Request
GET /api/rewards/all
Example
http://localhost:8080/api/rewards
Response

{
"customerId": 1,
"customerName": "John Doe",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 115
},
{
"month": "2026-05",
"rewardPoints": 250
}
],
"totalRewards": 365
}

2.Get Rewards By Customer ID
Request
GET /api/rewards/{customerId}
Example
http://localhost:8080/api/rewards/1
Response
{
"customerId": 1,
"customerName": "John Doe",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 115
},
{
"month": "2026-05",
"rewardPoints": 250
}
],
"totalRewards": 365
}

3.Get Rewards Between Specific Date 
Request
GET /api/rewards/date-range
Example
http://localhost:8080/api/rewards/date-range?startDate=2026-04-01&endDate=2026-04-30
Response
[
{
"customerId": 1,
"customerName": "John Doe",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 115
}
],
"totalRewards": 115
},
{
"customerId": 2,
"customerName": "Jane Smith",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 45
}
],
"totalRewards": 45
},
{
"customerId": 3,
"customerName": "Robert Brown",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 10
}
],
"totalRewards": 10
},
{
"customerId": 4,
"customerName": "Emily Davis",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 110
}
],
"totalRewards": 110
},
{
"customerId": 5,
"customerName": "Michael Wilson",
"monthlyRewards": [
{
"month": "2026-04",
"rewardPoints": 5
}
],
"totalRewards": 5
]

Exception Handling
The application uses global exception handling through:
@RestControllerAdvice

Customer Not Found

Response
{
"timestamp": "2026-06-12T10:00:00",
"status": 404,
"error": "Not Found",
"message": "Customer not found with ID"
}

Validation Error

Response
{
"timestamp": "2026-06-19T19:05:38.8991099",
"status": 400,
"error": "Bad Request - Validation Failed",
"message": "Customer ID must be greater than zero"
}

Invalid Endpoint

Response
{
"timestamp": "2026-06-19T19:06:14.038667",
"status": 404,
"error": "Not Found",
"message": "/api/rewards"
}

Internal Server Error

Response
{
"timestamp": "2026-06-12T10:00:00",
"status": 500,
"error": "Internal Server Error",
"message": "Unexpected Error"
}

Testing

The application includes comprehensive Unit and Integration Tests covering reward calculation logic, API endpoints, 
validation rules, exception handling,
and negative test scenarios to ensure reliability and correctness.

Test Coverage

Current Coverage:
Controller Layer: 100%
Service Layer: 90
Overall Coverage: ~90

Build Project
mvn clean install

Run Application
mvn spring-boot:run

H2 Database Console

URL
http://localhost:8080/h2-console

Configuration
JDBC URL : jdbc:h2:mem:rewardsdb
Username : sa
Password : 


