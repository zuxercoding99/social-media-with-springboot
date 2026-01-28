![Banner](docs/img/banner.jpg)

# Social Media Platform – Spring Boot & Vanilla Frontend

**A full-stack social media application (Twitter-like)** built as a personal project to demonstrate my skills in **backend development with Spring Boot 3** and a **Vanilla HTML/CSS/JS frontend**.

The application allows users to authenticate securely (JWT + OAuth2), create and interact with posts, chat in real time, manage friendships, and customize their profiles. It was designed with a strong focus on **security, performance, and production readiness**, including **backend observability through metrics and centralized logging** and is fully prepared for **Docker-based cloud deployment**.

This project showcases my ability to design, implement, and deploy a complete system end‑to‑end.

---

![Feed](docs/img/feed_v3.png)

## ✨ Features

- **Authentication & Authorization**
  - JWT-based authentication
  - OAuth2 login (Google)
  - Refresh tokens for secure session renewal
  - Role-based access control (USER / ADMIN)

- **Posts & Feed**
  - Create posts with text and images
  - Privacy levels: public, friends-only, private
  - Paginated feed, likes, and comments

- **Profiles**
  - Custom user profiles (bio, avatar, birthdate)
  - Persistent light/dark theme per user

- **Social Features**
  - Friend requests and relationships
  - Permission control per user and resource

- **Real-Time Chat**
  - Public chat room using WebSockets (STOMP)
  - Private messaging support

- **Media Handling**
  - Image uploads for posts and avatars (5MB limit)
  - File validation and previews

- **Security & Performance**
  - Password hashing with BCrypt
  - Rate limiting by IP (Bucket4j)
  - Caching with Caffeine
  - Configurable CORS

- **Admin & Observability**
  - Spring Boot Actuator endpoints for health checks and runtime insights
  - Micrometer metrics exported to Prometheus
  - Application and JVM metrics visualization via Grafana dashboards
  - Centralized application logging for production monitoring

- **Database & Deployment**
  - Flyway database migrations (production-ready)
  - Dockerized setup
  - CI/CD with GitHub Actions

---

## Tech Stack

### Backend

- **Java 17**
- **Spring Boot 3.5.x**
- Spring Web (REST API)
- Spring Data JPA (Hibernate)
- Spring Security (JWT + OAuth2)
- Spring WebSocket (STOMP)
- Spring Cache + Caffeine
- Bucket4j (rate limiting)
- Spring Actuator
- Spring Boot Actuator (health, metrics, monitoring)
- Micrometer (metrics instrumentation)
- Prometheus (metrics scraping)
- Grafana (metrics visualization)
- Centralized logging (Better Stack – SaaS)
- Springdoc OpenAPI (Swagger UI)
- Apache Tika (file validation)
- Flyway (DB migrations)

### Database

- **H2** – Development
- **PostgreSQL** – Production

### Frontend

- HTML5, CSS3 (responsive, light/dark mode)
- Vanilla JavaScript
- Fetch API
- Browser state via localStorage

### Testing

- JUnit 5
- Spring Boot Test

### Build & Config

- Gradle
- Profiles: `dev`, `test`, `prod`
- Environment-variable based configuration
- GitHub Actions CI/CD pipeline

---

## Local Setup

### Backend without docker

```bash
./gradlew build bootRun
```

Default active profile:

```text
dev
```

### Backend with docker

```bash
docker build -t social-media . && docker run -d -p 8080:8080 --name social-media-container social-media

```

Default active profile:

```text
dev
```

### Frontend

Use a local server with address localhost:5500 (e.g., Live Server).

---

## 📖 API Documentation

Swagger UI available at:

```text
/swagger-ui.html
```

---

## Try it online - Cloud Deployment

🔗 **Demo Online:**

> [https://social-media-with-springboot-frontend.onrender.com/](https://social-media-with-springboot-frontend.onrender.com/)

---

## 👨‍💻 Author

Developed by **zuxercoding99**

Focus: Backend & Full-Stack Java

## Contact

- Email: zkcoding99@gmail.com
- LinkedIn: https://www.linkedin.com/in/ezequiel-zk993213/

---

If you find this project interesting, feel free to give it a star! ⭐

## 📸 Screenshots

![Index](docs/img/index_v2.png)
![Register](docs/img/register_v2.png)
![Login](docs/img/login_v2.png)
![Feed](docs/img/feed_v3.png)
![Feed](docs/img/feed2.png)
![Profile](docs/img/user.png)
![Post](docs/img/post.png)
![Post](docs/img/post2.png)
![Chat](docs/img/chat.png)
![Chat](docs/img/chat2.png)
![Options](docs/img/options.png)
![Friend](docs/img/friends.png)
![Edit](docs/img/editar.png)
![Edit](docs/img/editar2.png)
![Request](docs/img/requests.png)

## Refresh token working

![Request](docs/img/refresh.png)

## Centralized logging using Better Stack (SaaS)

![Request](docs/img/logs.png)
