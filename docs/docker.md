# Docker

Este proyecto ya puede correr en Docker sin depender de tu entorno local.

## Qué incluye

- Imagen Docker del backend Spring Boot.
- `docker-compose.yml` para levantar el backend junto con PostgreSQL.
- Configuración por variables de entorno para:
  - base de datos,
  - servicio FastAPI/Uvicorn,
  - frontend Vue,
  - CORS,
  - JWT,
  - Groq/OpenAI,
  - uploads.

## Estructura

- `Dockerfile`: construye la imagen del backend.
- `docker-compose.yml`: levanta backend y PostgreSQL.
- `.env.example`: variables que debes copiar a `.env` y adaptar.
- `uploads/`: volumen para persistir imágenes cargadas.

## Requisitos

- Docker Engine o Docker Desktop.
- Docker Compose v2.
- Archivo `.env` completo en la raiz del proyecto.

## Uso local

1. Copia `.env.example` a `.env`.
2. Ajusta los valores que necesites.
3. Ejecuta:

```bash
docker compose up -d --build
```

4. Revisa logs:

```bash
docker compose logs -f backend
```

5. Abre el backend en:

```text
http://localhost:8080
```

Si falta una variable en `.env`, el backend no arranca porque ya no hay defaults en `application.yaml`.

Si ejecutas el backend desde IntelliJ, `DATABASE_URL` debe apuntar a `localhost`. Si lo ejecutas dentro de Docker, el `docker-compose.yml` ya lo cambia a `db` para el contenedor.

## Servicios externos

Si tu FastAPI/Uvicorn y tu Vue están fuera de este `docker-compose`, define:

- `CONSUMO_ML_URL`: URL pública del FastAPI.
- `APP_FRONTEND_BASE_URL`: URL pública del frontend Vue.
- `APP_CORS_ALLOWED_ORIGINS`: origen permitido del frontend.

Ejemplo:

```bash
CONSUMO_ML_URL=https://ml.tudominio.com
APP_FRONTEND_BASE_URL=https://app.tudominio.com
APP_CORS_ALLOWED_ORIGINS=https://app.tudominio.com
```

## Despliegue en un VPS

1. Instala Docker Engine y Docker Compose en el VPS.
2. Sube este repositorio al servidor o copia solo `docker-compose.yml`, `Dockerfile`, `uploads/` y `.env`.
3. Crea el archivo `.env` en el VPS con tus valores reales.
4. Ejecuta desde la carpeta del proyecto:

```bash
docker compose up -d --build
```

5. Si vas a usar dominio, pon un proxy inverso como Nginx o Traefik delante del puerto `8080`.

## Flujo Con Docker Desktop

Si ya tienes Docker Desktop en tu PC, puedes preparar la imagen localmente y moverla al VPS de dos formas.

### Opcion A: Registry

1. Construye la imagen:

```bash
docker build -t tuusuario/inventario-backend:1.0 .
```

2. Publica la imagen:

```bash
docker push tuusuario/inventario-backend:1.0
```

3. En el VPS:

```bash
docker pull tuusuario/inventario-backend:1.0
docker compose up -d
```

En este repositorio, el backend usa `image: ${BACKEND_IMAGE}` en `docker-compose.yml`, asi que debes poner en `.env` el mismo nombre de imagen que publicaste en Docker Hub.

### Opcion B: Archivo Tar

1. Exporta la imagen:

```bash
docker save -o inventario-backend.tar inventario-backend
```

2. Copia `inventario-backend.tar` al VPS.
3. Importa la imagen en el VPS:

```bash
docker load -i inventario-backend.tar
```

4. Levanta el stack con tu `.env`:

```bash
docker compose up -d
```

## Persistencia

- PostgreSQL guarda datos en el volumen `postgres_data`.
- Las imágenes subidas quedan en `./uploads` del host, montado dentro del contenedor.

## Imagen manual

Si prefieres una sola imagen del backend:

```bash
docker build -t inventario-backend .
docker run --rm -p 8080:8080 --env-file .env inventario-backend
```

## Compose con Docker Hub

Si ya publicaste la imagen, el despliegue normal es:

```bash
docker compose pull
docker compose up -d
```

Antes de eso, verifica que `BACKEND_IMAGE` en `.env` apunte a tu repo de Docker Hub, por ejemplo:

```env
BACKEND_IMAGE=tuusuario/inventario-backend:1.0
```

## Notas

- En producción, no dejes secretos reales en el archivo del repositorio.
- Usa variables de entorno reales en tu host.
- Si cambias la URL pública del frontend, actualiza `APP_FRONTEND_BASE_URL` para que los enlaces que genera el backend sigan siendo correctos.
