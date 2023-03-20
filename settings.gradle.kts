rootProject.name = "dataland"
include(
    "dataland-backend-utils",
    "dataland-backend",
    "dataland-api-key-manager",
    "dataland-internal-storage",
    "dataland-e2etests",
    "dataland-frontend",
    "dataland-csvconverter",
    "dataland-keycloak:dataland_theme:login",
    "dataland-keycloak-adapter",
    "dataland-qa-service",
    "dataland-message-queue-utils",
    "dataland-document-manager",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("springdoc-openapi-ui", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.9.2")
            library("junit-jupiter-engine", "org.junit.jupiter:junit-jupiter-engine:5.9.2")
            library("junit-jupiter-api", "org.junit.jupiter:junit-jupiter-api:5.9.2")

            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.14.0")
            library("moshi-adapters", "com.squareup.moshi:moshi-adapters:1.14.0")

            library("swagger-jaxrs2-jakarta", "io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.2")
            library("swagger-gradle-plugin", "io.swagger.core.v3:swagger-gradle-plugin:2.2.2")
            library("swagger-annotations", "io.swagger.core.v3:swagger-annotations:2.2.2")

            library("okhttp", "com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
            library("rs-api", "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

            library("dataland-edc-client", "org.dataland:dataland-edc-client:0.2.9")

            library("log4j", "org.apache.logging.log4j:log4j:2.20.0")
            library("log4j-api", "org.apache.logging.log4j:log4j-api:2.20.0")
            library("log4j-to-slf4j", "org.apache.logging.log4j:log4j-to-slf4j:2.20.0")

            library("logback-classic", "ch.qos.logback:logback-classic:1.4.5")
            library("logback-core", "ch.qos.logback:logback-core:1.4.5")

            library("slf4j-api", "org.slf4j:slf4j-api:2.0.6")

            library("jackson-dataformat-csv", "com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.14.2")
            library("jackson-databind", "com.fasterxml.jackson.core:jackson-databind:2.14.2")
            library("jackson-datatype-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
            library("jackson-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")

            library("bcpkix-jdk15on", "org.bouncycastle:bcpkix-jdk15on:1.70")
            library("bcprov-jdk15on", "org.bouncycastle:bcprov-jdk15on:1.70")

            library("cloudevents-api", "io.cloudevents:cloudevents-api:2.4.2")
            library("cloudevents-json-jackson", "io.cloudevents:cloudevents-json-jackson:2.4.2")
            library("cloudevents-core", "io.cloudevents:cloudevents-core:2.4.2")
            library("spring-cloud-function-context", "org.springframework.cloud:spring-cloud-function-context:4.0.1")
            library("mailjet-client", "com.mailjet:mailjet-client:5.2.2")
        }
    }
}
