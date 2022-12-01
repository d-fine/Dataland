## Keycloak Adapter
This librarary encapsulates everything that is needed to integrate Keycloak with a Spring Service.

## Usage
To use this library, do the following:

Add to `build.gradle.kts` of the service:
```
    implementation(project(":dataland-keycloak-adapter"))
```

Annotate the main spring application class with
```
@ComponentScan(basePackages = ["org.dataland"])
```

Adapt the application.properties. Adapt publiclinks to the needs of your service:
```
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:8080/keycloak/realms/datalandsecurity
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://keycloak:8080/keycloak/realms/datalandsecurity/protocol/openid-connect/certs
dataland.authorization.publiclinks=/actuator/health,/actuator/health/ping,/actuator/info,/swagger-ui/**,/v3/api-docs/**,/companies/**,/data/**,/metadata,/metadata/**
```
