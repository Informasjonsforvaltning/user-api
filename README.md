This API provides SSO with additional information and function. It proxies user data from Altinn, through module "fdk-nginx-altinn-proxy", maps external roles to associated FDK-roles and gets information regarding FDK terms and conditions related to the users associated organizations.

## Requirements
- maven
- java 21
- docker
- docker-compose

## Run tests
```
mvn verify
```

## Run locally
```
docker-compose up -d
mvn spring-boot:run -Dspring.profiles.active=develop
```
