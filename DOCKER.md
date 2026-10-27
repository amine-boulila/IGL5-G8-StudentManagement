# Run the application with Docker Compose

Docker Compose starts the Spring Boot application and its MySQL database.
The database is stored in a named volume and is not published to a host port.
The Swagger UI is available at
[http://localhost:8089/student/swagger-ui/index.html](http://localhost:8089/student/swagger-ui/index.html).
For example, list students at
`http://localhost:8089/student/students/getAllStudents`.

## Start

From the repository root in PowerShell:

```powershell
Copy-Item .env.example .env
```

For local development, the example values work but are not suitable for a
shared, public, or production environment. You can edit `.env` before starting;
it is excluded from Git. `MYSQL_USER` must be a non-root application account
(for example, `studentmanagement`); the MySQL image does not allow
`MYSQL_USER=root`. `MYSQL_ROOT_PASSWORD` is configured separately.

```powershell
docker compose up --build -d
docker compose ps
docker compose logs -f app
```

The application starts after MySQL passes its health check. The first image
build runs the Maven tests and packages the application. Check the service
states with `docker compose ps`; MySQL should report `healthy`.

## Stop

```powershell
docker compose down
```

The database volume is retained when stopping. To delete the database and all
its local data as well:

```powershell
docker compose down -v
```

The `-v` option is destructive: it removes the local MySQL data.
