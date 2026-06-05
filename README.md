# Inventario Chatbot Backend

Backend Spring Boot para inventario con chatbot, autenticación JWT, PostgreSQL, uploads de imágenes e integración con un servicio ML/FastAPI.

## Docker

La forma recomendada de llevarlo a cualquier host es usar Docker.

Ver detalles en [docs/docker.md](docs/docker.md).

## Arranque local

```bash
./mvnw test
./mvnw spring-boot:run
```

## Variables importantes

- `DATABASE_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET_KEY`
- `GROQ_API_KEY`
- `CONSUMO_ML_URL`
- `APP_FRONTEND_BASE_URL`
- `APP_CORS_ALLOWED_ORIGINS`
