# 🥋 Martial Arts App

A full-stack web application where martial artists build a personal **library of techniques**
(each with an uploaded demonstration video), **favorite** other users' techniques, and discuss
in a **community forum**. Built as a capstone project — a React single-page front end backed by
a Spring Boot REST API and a MySQL database.

🎥 **Video demo:** https://www.youtube.com/watch?v=rS6CwDShNbY

## Tech stack

| Layer    | Technologies                                                        |
|----------|---------------------------------------------------------------------|
| Frontend | React (Create React App), React Router, Fetch API, CSS              |
| Backend  | Java, Spring Boot, Spring Data JPA / Hibernate                      |
| Database | MySQL / MariaDB (technique videos stored as BLOBs)                  |
| Build    | Maven (backend), npm (frontend)                                     |

## Features

- **Technique Library** — create techniques (name, type, description, video) and favorite others'.
- **Video upload & playback** — videos uploaded as `multipart/form-data`, stored as BLOBs, and
  streamed back as MP4 from a dedicated endpoint.
- **Forum** — browse the most popular and the 10 newest techniques across all users.
- **Dev Blog & Feedback** — message-based pages for announcements and user feedback.
- **Auth** — user registration and login.

## Architecture

```
MartialArtsBackend/     Spring Boot REST API (controllers, JPA entities, repositories)
  └─ com.alijah.martial_arts_app
       controllers/     TechniqueController, MessageController, UserController
       models/          Technique, Message, User, TechniqueResponse (custom query POJO)
       repositories/    Spring Data JPA repositories
martial_arts_frontend/  React SPA (Pages: Library, Forum, DevBlog, Feedback, About, Login, Register)
Wireframe/              UI wireframes
martialArtsSchema.png   Database schema
```

### Data model

![Database schema](martialArtsSchema.png)

## REST API (selected endpoints)

| Method | Endpoint                                   | Purpose                              |
|--------|--------------------------------------------|--------------------------------------|
| POST   | `/technique`                               | Create a technique (with video)      |
| GET    | `/technique/{username}`                    | A user's techniques                  |
| GET    | `/technique/popular` · `/technique/latest` | Most-favorited · 10 newest           |
| POST   | `/user` · `/user/fav/{username}/{id}`      | Register · favorite a technique      |
| GET    | `/user/{username}/{password}`              | Login lookup                         |
| POST   | `/message` · GET `/message/received/{user}`| Forum / dev-blog / feedback messages |

## Running locally

**Backend** (needs a local MySQL/MariaDB):
```bash
cd MartialArtsBackend
# connects to jdbc:mysql://localhost:3306/ma_db (auto-creates the DB)
./mvnw spring-boot:run
```
Set `MYSQL_HOST` and the datasource username/password in
`src/main/resources/application.properties` to match your local DB.

**Frontend:**
```bash
cd martial_arts_frontend
npm install
npm start          # http://localhost:3000
```

## Screens (wireframes)

| Forum | Login |
|---|---|
| ![Forum](Wireframe/Forum.jpg) | ![Login](Wireframe/Login.jpg) |
| ![Feedback](Wireframe/Feedback.jpg) | ![Register](Wireframe/Register.jpg) |

## Engineering notes & challenges

A few problems worth calling out from building this:

- **Video upload → BLOB → playback.** File uploads need `multipart/form-data` and a `FormData`
  object on the client (not a JSON field); on the server the file arrives as a `MultipartFile`,
  is converted to bytes, and stored as a BLOB. Keeping the conversion in the controller (rather
  than in entity setters) kept the JPA entity clean and reduced errors. A dedicated endpoint
  returns *only* the video as MP4 so the resource plays directly in the browser.
- **Custom POJO for HQL join results.** Aggregated/joined query results didn't map onto a single
  entity, so I introduced a `TechniqueResponse` projection class and joined across `@ManyToMany`
  relationships in HQL rather than raw SQL.
- **Async fetch + React state.** Features that depend on server data required `async/await` on the
  fetch calls and storing results in component state, plus status-code-aware error handling, to get
  predictable UI behavior.
- **Learning React.** First substantial React project — learned the component/state model and
  lifecycle updates to re-render pages as fetched data arrived.
