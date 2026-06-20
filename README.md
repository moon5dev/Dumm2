# Dumm2 Projects

`Dumm2` is a prototype collection repository. The name is read as "Dumm-E"
in Korean, using the number `2` as the sound "이".

This repository is used to keep small full-stack experiments together while
trying out backend patterns, data modeling, UI screens, and system integration
ideas.

## Project Structure

```text
Dumm2/
├── board/                # Spring Boot board/admin prototype
├── label-printer/
│   ├── web/              # Spring Boot label template and print queue server
│   └── Client/           # .NET console client that polls and generates ZPL
├── sap-connector/        # Spring Boot SAP connector prototype skeleton
└── docs/                 # Notes and ERD artifacts
```

## Projects

### board

A Spring Boot prototype for a simple board/admin system.

- Java 17
- Spring Boot 3.5.x
- Spring Web, Spring Data JPA, Spring Security
- Thymeleaf server-rendered admin pages
- MySQL for local runtime
- H2 for tests

Current focus:

- User CRUD
- Login/admin page protection
- Basic board and post domain modeling
- JPA auditing examples

### label-printer

A prototype for creating label templates in a web UI and sending print jobs to
a local client.

`label-printer/web`:

- Java 17
- Spring Boot 4.0.x
- Spring Web MVC, Spring Data JPA, Validation
- Thymeleaf page with Kendo UI and Fabric.js
- SQL Server local database
- Print queue API for client polling

`label-printer/Client`:

- .NET 8 console app
- Polls pending print jobs from the web server
- Converts template JSON and print data into ZPL
- Saves generated ZPL files locally

### sap-connector

A Spring Boot prototype skeleton for future SAP integration experiments.

- Java 17
- Spring Boot 4.0.x
- Spring Web MVC
- Validation

## Local Development Notes

Each Java project is currently an independent Gradle project with its own
wrapper.

```bash
./board/gradlew -p board test
./label-printer/web/gradlew -p label-printer/web test
./sap-connector/gradlew -p sap-connector test
```

The Java projects require a local JDK 17 runtime.

Runtime database credentials are read from environment variables such as
`DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`, and, for the SQL Server Docker
setup, `MSSQL_SA_PASSWORD`.

## Documentation

- `docs/erd/board.dbml` contains the current board ERD source.
- `docs/erd/board.svg` and `docs/erd/board.png` contain rendered board ERD
  images.

## Repository Status

This repository is intentionally prototype-oriented. Some modules are more
complete than others, and documentation may lag behind experiments while ideas
are being tested.
