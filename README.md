# 🎯 Retailer Rewards Program

A Spring Boot REST API application that calculates and tracks customer reward points based on purchase transactions.

The application retrieves customer transactions from the database, calculates reward points according to configurable business rules, aggregates rewards monthly, computes total rewards per customer, and exposes the results through REST APIs.

---

## 🚀 Features

✅ Reward points calculation based on transaction amount

✅ Monthly and total reward aggregation

✅ Customer-specific reward summaries

✅ Date-range based reward reporting

✅ Configurable reward calculation period

✅ Global exception handling

✅ Request validation

✅ Automated sample data loading using SQL scripts

✅ H2 In-Memory Database support

✅ Comprehensive Unit & Integration Tests

---

## 📋 Business Rules

Customers earn reward points based on purchase amount:

| Purchase Amount | Reward Points |
| --------------- | ---------------------------------------------------------------------------- |
| Up to $50       | 0 Points |
| $50 - $100      | 1 Point per $1 spent above $50 |
| Above $100      | 2 Points per $1 spent above $100 + 1 Point per $1 spent between $50 and $100 |

### Example

**Purchase Amount = $120**

Reward Points:

* 50 points for spending between $50 and $100
* 40 points for spending above $100

**Total = 90 Points**

```text
(20 × 2) + 50 = 90
```

---

## 🏗️ Technology Stack

| Technology      | Version            |
| --------------- | ------------------ |
| Java            | 17                 |
| Spring Boot     | 3.5.0              |
| Spring Data JPA | Latest             |
| Hibernate       | Latest             |
| H2 Database     | In-Memory          |
| Maven           | Build Tool         |
| Lombok          | Latest             |
| JUnit 5         | Testing            |
| Mockito         | Mocking            |
| MockMvc         | Controller Testing |

---

## 📂 Project Structure

```text
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
│   │       ├── repository
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
```

---

## 🗄️ Data Model

### Customer

| Field        | Type   |
| ------------ | ------ |
| customerId   | Long   |
| customerName | String |

### Transaction

| Field           | Type       |
| --------------- | ---------- |
| id              | Long       |
| customer        | Customer   |
| amount          | BigDecimal |
| transactionDate | LocalDate  |

### Relationship

```text
Customer (1) -------- (*) Transaction
```

* One Customer can have multiple Transactions
* One Transaction belongs to one Customer

---

## 🔗 REST API Endpoints

### 1️⃣ Get Rewards for All Customers

```http
GET /api/rewards/all
```

#### Sample Response

```json
[
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
]
```

---

### 2️⃣ Get Rewards by Customer ID

```http
GET /api/rewards/{customerId}
```

#### Example

```http
GET /api/rewards/1
```

#### Sample Response

```json
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
```

---

### 3️⃣ Get Rewards by Date Range

```http
GET /api/rewards/date-range?startDate=2026-04-01&endDate=2026-04-30
```

#### Sample Response

```json
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
  }
]
```

---

## ⚙️ Configuration

Reward calculation period is configurable through:

```properties
rewards.calculation.months=3
```

This allows changing the reward calculation window without modifying code.

---

## ⚠️ Exception Handling

Implemented using:

```java
@RestControllerAdvice
```

### Customer Not Found

```json
{
  "timestamp": "2026-06-12T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with ID"
}
```

### Validation Error

```json
{
  "timestamp": "2026-06-19T19:05:38.8991099",
  "status": 400,
  "error": "Bad Request - Validation Failed",
  "message": "Customer ID must be greater than zero"
}
```

### Invalid Endpoint

```json
{
  "timestamp": "2026-06-19T19:06:14.038667",
  "status": 404,
  "error": "Not Found",
  "message": "/api/rewards"
}
```

### Internal Server Error

```json
{
  "timestamp": "2026-06-12T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Unexpected Error"
}
```

---

## 🧪 Testing

The application includes:

* Unit Tests
* Integration Tests
* Validation Tests
* Exception Handling Tests
* Controller Layer Tests
* Service Layer Tests
* Negative Scenario Tests

### Test Coverage

| Layer            | Coverage |
| ---------------- | -------- |
| Controller Layer | 100%     |
| Service Layer    | 90%      |
| Overall Coverage | ~90%     |

---

## 🛠️ Build & Run

### Build Project

```bash
mvn clean install
```

### Run Application

```bash
mvn spring-boot:run
```

Application will start on:

```text
http://localhost:8080
```

---

## 🗃️ H2 Database Console

### Access URL

```text
http://localhost:8080/h2-console
```

### Configuration

```text
JDBC URL : jdbc:h2:mem:rewardsdb
Username : sa
Password :
```

---

## 📌 Assumptions

* Transaction amounts cannot be negative.
* Customers may exist without transactions.
* Customer must exist before reward retrieval.
* Rewards are aggregated monthly and overall.
* Date range searches return transactions only within the provided range.
* Sample data is loaded using SQL scripts.
* H2 Database is used for persistence.
* Validation and exception handling ensure meaningful API responses.
