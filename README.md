# Movie Library REST API

A secure Spring Boot REST API for managing a digital movie library, with role-based access control and automatic IMDb rating enrichment via the OMDb API.

## Features

- **Role-based access control** — endpoints restricted by `USER` and `ADMIN` roles
- **Asynchronous OMDb integration** — new movies are enriched with IMDb ratings in the background, without blocking the save request
- **Advanced filtering** — dynamic queries let admins search users by optional criteria (username, first name, last name)
- **Secure credential handling** — BCrypt password hashing, robust JSON deserialization
- **Interactive API docs** — Swagger UI for exploring and testing endpoints directly

## Tech Stack

- **Backend:** Java 17, Spring Boot (Web, Security, Data JPA)
- **Database:** MariaDB, Hibernate
- **Auth:** Spring Security — HTTP Basic authentication, session-based (JSESSIONID), BCrypt password hashing
- **API Docs:** Swagger / OpenAPI 3.0
- **External API:** OMDb (Open Movie Database)
- **Build:** Gradle
- **Other:** Lombok

## Architecture & Design Decisions

The reasoning behind the async OMDb integration, the RestClient-vs-WebClient trade-off, and the session-based auth model is documented separately in [ADR.md](docs/ADR.md) — worth a read if you want the "why," not just the "what."

## Getting Started

### Quick Start (Docker)

The fastest way to run the app — spins up the application together with a disposable local MariaDB database, pre-seeded with demo users and movies. You only need to supply one thing yourself: a free OMDb API key (rating lookups are a third-party, per-account service, so this can't be provided for you).

```bash
git clone https://github.com/stoyanovse/movie-library.git
cd movie-library
cp .env.example .env
```

Get a free key at [omdbapi.com/apikey.aspx](https://www.omdbapi.com/apikey.aspx), then open `.env` and set:
```
OMDB_API_KEY=your-key-here
```

Then:
```bash
docker compose up --build
```

The API will be available at **http://localhost:8080**, with interactive docs at **http://localhost:8080/swagger-ui/index.html**.

On first run, the app seeds a few demo users and movies, then automatically triggers the real asynchronous OMDb rating fetch for each seeded movie — so within a few seconds of startup you'll see ratings appear, demonstrating the app's core feature end-to-end.

### Demo Accounts

| Role  | Username | Password       |
|-------|----------|----------------|
| Admin | `admin`  | `Password123!` |
| User  | `demo`   | `Password123!` |

Authenticate against protected endpoints via Swagger's "Authorize" button (HTTP Basic).

### Manual Setup (without Docker)

<details>
<summary>Requires your own MariaDB instance</summary>

1. Install and run MariaDB locally, and create the database:
   ```sql
   CREATE DATABASE movie_library;
   ```
2. Set the following environment variables (or an IDE run configuration):

   | Variable | Description |
      |---|---|
   | `DB_URL` | e.g. `jdbc:mariadb://localhost:3306/movie_library` |
   | `DB_USERNAME` | your MariaDB username |
   | `DB_PASSWORD` | your MariaDB password |
   | `OMDB_API_KEY` | your free OMDb API key |

3. Run:
   ```bash
   ./gradlew bootRun
   ```

</details>

### Environment Variables (Docker)

| Variable | Description | Default |
|---|---|---|
| `DB_NAME` | Local MariaDB database name | `movie_library` |
| `DB_USER` | Local MariaDB user | `movielibrary` |
| `DB_PASSWORD` | Local MariaDB password | `movielibrary` |
| `DB_ROOT_PASSWORD` | Local MariaDB root password | `rootpassword` |
| `OMDB_API_KEY` | Your personal OMDb API key | _(required, no default)_ |

## Database Schema

![Database Schema](docs/images/schema.png)

## Testing

```bash
./gradlew test
```


## License

This project is licensed under the MIT License.
