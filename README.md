# User API

This application provides an API that provides SSO with additional information and function. It proxies user data from
Altinn, through module ```fdk-nginx-altinn-proxy```, maps external roles to associated FDK-roles and gets information
regarding FDK terms and conditions related to the users associated organizations.

For a broader understanding of the system’s context, refer to
the [architecture documentation](https://github.com/Informasjonsforvaltning/architecture-documentation) wiki. For more
specific context on this application, see the **IAM** subsystem section.

## Getting Started

These instructions will give you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

Ensure you have the following installed:

- Java 21
- Maven
- Docker

### Running locally

Clone the repository

```sh
git clone https://github.com/Informasjonsforvaltning/user-api.git
cd user-api
```

#### Start proxy and the application (either through your IDE using the dev profile, or via CLI):

```sh
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```

### API Documentation (OpenAPI)

The API documentation is available at ```src/main/resources/specification```.

### Running tests

```sh
mvn verify
```
