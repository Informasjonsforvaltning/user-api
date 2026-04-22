# User API

This application provides an API for SSO with additional user information. It uses Altinn Access Management
(authorized parties), maps external roles to FDK roles, and provides FDK terms and conditions for the user’s organizations.

For a broader understanding of the system’s context, refer to
the [architecture documentation](https://github.com/Informasjonsforvaltning/architecture-documentation) wiki. For more
specific context on this application, see the **IAM** subsystem section.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing.

### Prerequisites

- Java 21
- Maven
- Docker (optional, for local services)

### Running locally

```sh
git clone https://github.com/Informasjonsforvaltning/user-api.git
cd user-api
```

Run with the develop profile:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```

### API Documentation (OpenAPI)

The API documentation is available at ```src/main/resources/specification```.

### Running tests

```sh
mvn verify
```
