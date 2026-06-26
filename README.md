# DoseHope — Local development & quick deploy

This document shows quick commands to run the backend and a local MySQL instance via Docker Compose.

Prerequisites:

- Docker & Docker Compose installed
- (Optional) Java 17 + Maven if you prefer running locally without Docker

Quick start with Docker:

```bash
# from repository root
docker-compose up --build
```

This will start MySQL and the backend. The backend will be available at `http://localhost:8080` and seeds demo users/medicines on first start.

Demo credentials seeded:

- donor: donor@example.com / demo123
- ngo: ngo@example.com / demo123
- volunteer: volunteer@example.com / demo123
- admin: admin@example.com / admin123

Frontend development:

- The frontend is static files in `/frontend`. To test against the running backend, set `API_BASE` in `frontend/data-store.js` to `http://localhost:8080` (or keep it empty if you serve static files from the same origin as the backend).

Run backend locally (without Docker):

```bash
cd backend
mvn -DskipTests spring-boot:run
```

Notes:

- `application.properties` reads many values from environment variables for safer deployment.
- Before deploying to production, replace `JWT_SECRET` with a secure random value and lock down DB credentials.
