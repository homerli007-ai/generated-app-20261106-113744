# Safe File Deleter

A small full-stack demo application for safely deleting files on a server. The backend exposes a single REST endpoint that only deletes regular files located inside a configured `allowed_root` directory, refusing symlinks, directories, and any path that escapes the root. The frontend provides a simple UI to submit a list of relative paths and view per-path results.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx
        └── App.css
```

## How to run

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. The allowed root directory defaults to `./allowed_root` (relative to the backend working directory) and can be overridden via `app.allowed-root` in `application.properties` or as a JVM/system property.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` requests to the backend on port `8080`.

## API

### `POST /api/delete`

Deletes one or more files relative to the configured `allowed_root`.

**Request body**

```json
{
  "paths": ["one/path.txt", "another/file.log"]
}
```

**Response**

```json
{
  "results": [
    { "path": "one/path.txt", "status": "deleted" },
    { "path": "another/file.log", "status": "refused", "reason": "not found" }
  ]
}
```

Each result has a `status` of `deleted`, `refused`, or `error`, and an optional `reason` explaining refusals or failures.

**Refusal conditions**

- Empty or blank path
- Path resolves outside `allowed_root`
- Target is a symbolic link
- Target does not exist
- Target is not a regular file (e.g. a directory)

## Notes

- The `allowed_root` directory must exist and be accessible; otherwise the endpoint returns HTTP 500.
- Paths are resolved with `normalize()` and checked with `startsWith(root)` after resolving the root via `toRealPath()`. This protects against `..` traversal, but does not attempt to defend against TOCTOU races between the check and the delete.
- Symlinks are refused outright (both as the target and, implicitly, as intermediate components resolved by the filesystem).
- There is no authentication or authorization — this is a demo and should not be exposed publicly as-is.
- Deletion is permanent; there is no trash or undo.
- The frontend has no build-time configuration for the API base URL; it relies on the Vite dev proxy. For production, serve the built frontend behind a reverse proxy that forwards `/api` to the backend, or adjust `vite.config.js` accordingly.
- No persistence layer, tests, or CI configuration are included.