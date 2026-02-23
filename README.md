# 🔐 Auth-Service: Secure Identity & Access Management

This is a robust **Java Backend** application focused on secure authentication and authorization. It implements modern security standards like **JWT** and provides a second layer of security through **OTP (One-Time Password)** via Gmail integration.

## 🌟 Key Features

* **JWT Authentication**: Secure, stateless user authentication using JSON Web Tokens.
* **OTP via Gmail (SMTP)**: Multi-factor authentication support by sending one-time passwords to users' emails.
* **Role-Based Access Control (RBAC)**: Fine-grained authorization using Enums for different user roles (Admin, User, etc.).
* **DTO Pattern & Clean Code**: High maintainability using **Java 21 Records** for Data Transfer Objects and a clear separation of concerns.
* **RESTful API Design**: Well-structured endpoints for user registration, login, and profile management.

## 🛠 Tech Stack

* **Language**: Java 21 (utilizing modern features like Records)
* **Framework**: Spring Boot 3.x
* **Security**: Spring Security & JWT
* **Database**: MySQL / PostgreSQL
* **Mail Service**: Spring Boot Starter Mail (Gmail SMTP)
* **Containerization**: Docker & Docker Compose

## 🐳 Docker Quick Start

To run the entire authentication system (Backend + Database) with a single command:

```bash
docker-compose up -d --build

```

## 🚀 API Highlights

* `POST /api/auth/register` : User registration and OTP trigger.
* `POST /api/auth/login` : Identity verification and JWT issuance.
* `POST /api/auth/verify-otp` : Secure OTP validation.
