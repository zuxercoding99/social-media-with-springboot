# Social Media Platform – Spring Boot & Vanilla Frontend

**A full-stack social media application** built as a personal project to demonstrate my skills in backend development with **Spring Boot 3 and HTML, CSS and Vanilla JS.** This app enables users to register, post content with privacy controls, like and comment on posts, engage in real-time chat, and customize profiles with themes and avatars. Designed with a focus on security, performance, and scalability, it's ready for cloud deployment.

This project highlights my ability to build a complete application from scratch, leveraging AI-assisted development to accelerate frontend implementation efficiently, even with foundational knowledge in UI design and frontend programming. I'm eager to secure my first software engineering role—let's connect!

---

## Features

- **User Authentication:** Secure registration and login using JWT tokens, with role-based access (user/admin).
- **Posts & Feed:** Create posts with text, images, and privacy levels (public, friends, private); paginated feed, likes, and comments.
- **Profiles:** Customizable user profiles including bio, avatar uploads, birthdate, and theme modes (light/dark).
- **Real-Time Chat:** Public chat room via WebSockets (STOMP), with support for private messaging.
- **Media Handling:** Image uploads for posts and avatars (5MB limit), previews, and lightbox viewer.
- **Themes & Effects:** Persistent light/dark modes using CSS variables, responsive design, and subtle animations like snowflake effects.
- **Security & Performance:** Rate limiting, caching, password hashing (BCrypt), configurable CORS, and monitoring via Actuator.
- **Admin Tools:** Automatic admin user seeding on startup, with Prometheus metrics exposure.

---

## Tech Stack

### Backend

- **Java 17** – Lenguaje principal
- **Spring Boot 3.5.x** – Framework base
- **Spring Web (REST API)** – Controladores y endpoints
- **Spring Data JPA** – Persistencia y modelado de entidades
- **Spring Security** – Autenticación y autorización
- **JWT (jjwt)** – Seguridad basada en tokens
- **Bean Validation (Jakarta Validation)** – Validación de datos
- **Spring WebSocket** – Comunicación en tiempo real
- **Spring Cache + Caffeine** – Cacheo en memoria
- **Bucket4j** – Rate limiting (protección contra abuso)
- **Spring Actuator** – Métricas y health checks
- **Micrometer + Prometheus** – Observabilidad y métricas
- **Springdoc OpenAPI** – Documentación automática de la API (Swagger UI)
- **Apache Tika** – Análisis y validación de archivos

### Base de Datos

- **H2** – Entorno de desarrollo
- **PostgreSQL** – Entorno productivo
- **Hibernate (JPA Provider)** – ORM

### Testing

- **JUnit 5**
- **Spring Boot Test**

### Build & Configuración

- **Gradle**
- **Profiles (dev / test / prod)**
- **Variables de entorno** listas para cloud deployment

---

## Frontend

- **HTML5 semántico**
- **CSS3** (responsive, dark / light mode)
- **JavaScript (Vanilla)**
- **Fetch API** para comunicación con el backend
- Manejo de estado en el navegador (localStorage)

👉 The frontend was developed **without frameworks**, demonstrating an understanding of web fundamentals and with AI assistance.

---

## Seguridad

- Autenticación mediante **JWT**
- Protección de endpoints por roles
- Validación de ownership (solo el dueño puede modificar/eliminar recursos)
- Manejo correcto de errores (`401`, `403`, `404`)
- Rate limiting por IP

---

## Funcionalidades Principales

- Registro e inicio de sesión de usuarios
- Creación, visualización y eliminación de posts
- Sistema de comentarios
- Relaciones entre usuarios (social graph)
- Control de permisos por usuario
- Modo claro / oscuro persistente
- WebSockets para funcionalidades en tiempo real
- Documentación interactiva con Swagger

---

## Instalación y Ejecución local

### Backend

```bash
./gradlew build bootRun
```

Perfil activo por defecto:

```text
dev
```

### Frontend

Abrir directamente los archivos HTML con un servidor local como Live Server.

---

## Documentación de la API

Disponible en:

```text
/swagger-ui.html
```

---

## Preparado para la Nube

El proyecto está preparado para desplegarse en:

- Render

Incluye:

- Separación de perfiles
- Configuración por variables de entorno
- Base de datos productiva

🔗 **Demo Online:**

> [https://TU-LINK-AQUI](https://TU-LINK-AQUI)

---

## Metodología y Habilidades Complementarias

Además del desarrollo técnico, este proyecto demuestra:

- **Capacidad de diseño backend completo** desde cero
- **Uso estratégico de IA como herramienta de productividad**, aplicando _prompt engineering_ para:

  - Acelerar el desarrollo
  - Validar decisiones técnicas
  - Mejorar calidad de código

- **Comprensión de fundamentos de UI/UX**, incluso con conocimientos básicos de diseño frontend
- Capacidad de aprendizaje autónomo y mejora continua

> La IA fue utilizada como **asistente**, no como sustituto del criterio técnico.

---

## Roadmap / Mejoras Futuras

- Tests de integración más extensos
- Sistema de notificaciones
- Subida de imágenes a storage externo (S3)

---

## 👨‍💻 Autor

Desarrollado por **zuxercoding99**

Proyecto creado con foco en **primer empleo backend / full‑stack Java**.

---

⭐ Si este proyecto te resulta interesante, ¡no dudes en dejar una estrella!
