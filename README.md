# Campus Connect

A full-stack campus community platform that enables users to create, share, and moderate content within a structured and secure environment.

---

## Overview

Campus Connect is a modern web application designed for campus communities, providing a discussion forum with a complete content lifecycle, including submission, moderation, and notification. The system emphasizes security, scalability, and user experience through a decoupled frontend-backend architecture.

---

## Highlights

* Implemented **stateless authentication** using JWT for scalable and secure session management
* Designed a **role-based access control (RBAC)** system to separate user and administrator privileges
* Built a **moderation workflow** where all user-generated content requires administrative approval before publication
* Integrated an **email notification system** to inform users of content approval or rejection outcomes
* Developed an **OTP-based email verification mechanism** using Redis with expiration to enhance authentication security
* Optimized performance with **Redis caching**, reducing database load and improving response time
* Enabled **rich content creation** with a modern editor and emoji support to improve user engagement
* Implemented **cloud-based media storage** for user avatars and images using object storage services
* Deployed a **decoupled architecture** on cloud infrastructure for better scalability and maintainability

---

## Tech Stack

### Backend

* Spring Boot 3
* MyBatis (SQL-based object mapping)
* MySQL
* Redis
* JWT (Authentication & Authorization)
* Email Service

### Frontend

* Vue 3
* Vue Router (Single Page Application)
* Element Plus (UI Framework)
* Tiptap (Rich Text Editor)
* Emoji Mart (Emoji Support)

### Cloud & Deployment

* DigitalOcean (Cloud Hosting)
* Cloudflare R2 (Object Storage for media files)
* GitHub (Version Control & Repository Management)

---

## Architecture

The system follows a **frontend-backend separation architecture**:

* A Vue 3-based Single Page Application handles user interaction and routing
* A Spring Boot RESTful API manages business logic and data processing
* MyBatis is used for database interaction with MySQL
* Redis is used for caching and temporary data storage (e.g., OTP)
* Cloud object storage is used for handling user-uploaded media
* Email services are integrated for asynchronous user notifications

---

## Core Workflow

* Users register and verify their identity via email-based OTP
* Authenticated users can create and submit posts
* Submitted posts enter a moderation queue
* Administrators review and approve or reject content
* Users receive email notifications based on moderation results

---

## Deployment

The application is deployed on a cloud server with a decoupled frontend and backend setup, ensuring scalability, maintainability, and ease of future extension.

---

## Repository

GitHub: https://github.com/hbx70/Campus-Connect

---

## Live Demo

Campus Connect: https://campusconnect.one
