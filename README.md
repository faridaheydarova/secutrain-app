#  Training Platform

> Enterprise-grade employee security awareness training platform  
> inspired by KnowBe4 — built as a full-stack portfolio project.

---

##  Architecture
┌─────────────────┐ REST API ┌──────────────────────┐
│ React + Redux │ ←─────────────── │ Spring Boot 3 (Java) │
│ Tailwind CSS │ │ 51 REST Endpoints │
└─────────────────┘ └──────────┬───────────┘
│
┌──────────────────┼──────────────┐
│ │ │
┌──────▼─────┐ ┌────────▼────┐ ┌─────▼──────┐
│ PostgreSQL │ │ Active Dir. │ │ JWT Auth │
└────────────┘ └─────────────┘ └────────────┘


---

##  Tech Stack

| Layer       | Technology                              |
|-------------|----------------------------------------|
| Backend     | Java 17, Spring Boot 3.2, Spring Security |
| Database    | PostgreSQL, Spring Data JPA / Hibernate |
| Auth        | JWT (HS512), Active Directory (LDAP)    |
| Frontend    | React 18, Redux Toolkit, Tailwind CSS   |
| Build       | Gradle (Kotlin DSL)                    |
| DevOps      | Docker, Docker Compose, GitLab CI/CD   |
| Docs        | Swagger / OpenAPI 3                    |

---

##  Features

###  Authentication & Security
- JWT access + refresh token (15 min / 8 saat)
- Active Directory (LDAP) integration — Hybrid auth mode
- Role-based access control (ADMIN / USER)
- Real-time AD status check on every request
- 5-minute user status cache to reduce AD load
- Hardcoded secret detection in CI pipeline

###  Campaign Management
- Create, assign and track training campaigns
- Bulk user assignment
- Video library per campaign
- Completion rate tracking

###  Video Management
- Multipart file upload (up to 500MB)
- Drag & drop frontend upload
- View count tracking
- Active/inactive status

###  Progress Tracking
- Per-video watch duration tracking
- Resume from last position
- Completion percentage (95% = complete)
- Attempt counting

### Admin Dashboard
- User CRUD + role management
- Campaign & video analytics
- At-risk user detection
- Audit logging for all actions

---

## API Overview

POST /api/auth/login → JWT token
POST /api/auth/register

GET /api/admin/users → Paginated user list
POST /api/admin/users → Create user
PUT /api/admin/users/{id}/role → Change role

GET /api/admin/campaigns → All campaigns
POST /api/admin/campaigns → Create campaign
POST /api/admin/campaigns/{id}/assign → Assign to users

POST /api/admin/videos → Upload video
GET /api/admin/videos/{id}/stream → Stream video

POST /api/user/progress/{aId}/{vId}/start → Start video
PUT /api/user/progress/{aId}/{vId}/update → Update progress
POST /api/user/progress/{aId}/{vId}/complete → Complete video



---

## Quick Start

```bash
# 1. Clone
git clone https://github.com/yourusername/training-platform.git
cd training-platform

# 2. Environment
cp .env.example .env
# Fill in your values

# 3. Start
docker-compose up -d

# 4. Frontend
cd frontend && npm install && npm run dev

# Access
Backend API  → http://localhost:8080
Swagger UI   → http://localhost:8080/swagger-ui.html
Frontend     → http://localhost:5173
```

---

##  Project Structure
training-platform/
├── src/main/java/com/training/
│ ├── controller/ # 6 REST controllers
│ ├── service/ # Business logic
│ ├── repository/ # JPA repositories
│ ├── entity/ # DB models
│ ├── dto/ # Request/Response objects
│ ├── security/jwt/ # JWT filter & provider
│ └── config/ # Security, OpenAPI config
├── frontend/ # React application
├── docker-compose.yml
├── .env.example
└── .gitlab-ci.yml # SAST + IaC scanning


---

##  Security Notes

- All secrets via environment variables — no hardcoded values
- `.env` excluded from version control
- SAST scanning enabled via GitLab CI
- Passwords hashed with BCrypt
- CORS configured for known origins only

---

##  Screenshots

> *(Add screenshots here)*

| Login | Dashboard | Campaign Detail |
|-------|-----------|-----------------|
| ![](docs/login.png) | ![](docs/dashboard.png) | ![](docs/campaign.png) |
