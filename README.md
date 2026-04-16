# Xbatis-Admin

RBAC admin demo with a Spring Boot backend and a Vue 3 frontend.

## Structure

- `admin`: Spring Boot 3 + xbatis + PGSQL/MySQL API
- `admin-ui`: Vue 3 + Vite + ant-design-vue console

## Requirements

- Java 21 for backend build and runtime
- Node.js 20+ for frontend development

If Maven reports `UnsupportedClassVersionError`, confirm that `java -version` is Java 21 rather than an older JDK.

## Run

Backend:

```bash
cd admin
mvn spring-boot:run
```

Frontend:

```bash
cd admin-ui
npm install
npm run dev
```

The admin-ui dev server proxies `/api` requests to `http://localhost:8080`.

The script will start the backend if `http://127.0.0.1:8080` is not reachable, exercise the main login, dashboard, user, role, and menu flows, and delete any temporary role/menu data it creates.

Expected side effects:

- user `admin` and `ops` password will remain `123456`