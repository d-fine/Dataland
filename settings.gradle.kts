rootProject.name = "dataland"
include(
    "dataland-backend-utils",
    "dataland-backend",
    "dataland-api-key-manager",
    "dataland-internal-storage",
    "dataland-e2etests",
    "dataland-frontend",
    "dataland-keycloak:dataland_theme:login",
    "dataland-keycloak-adapter",
    "dataland-qa-service",
    "dataland-message-queue-utils",
    "dataland-document-manager",
    "dataland-batch-manager",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("springdoc-openapi-ui", "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.9.3")

            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.15.0")
            library("moshi-adapters", "com.squareup.moshi:moshi-adapters:1.15.0")

            library("okhttp", "com.squareup.okhttp3:okhttp:4.11.0")

            library("log4j", "org.apache.logging.log4j:log4j:2.20.0")
            library("log4j-api", "org.apache.logging.log4j:log4j-api:2.20.0")
            library("log4j-to-slf4j", "org.apache.logging.log4j:log4j-to-slf4j:2.20.0")

            library("logback-classic", "ch.qos.logback:logback-classic:1.4.8")
            library("logback-core", "ch.qos.logback:logback-core:1.4.8")

            library("slf4j-api", "org.slf4j:slf4j-api:2.0.7")

            library("jackson-dataformat-csv", "com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.2")
            library("jackson-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

            library("bcpkix-jdk15on", "org.bouncycastle:bcpkix-jdk15on:1.70")
            library("bcprov-jdk15on", "org.bouncycastle:bcprov-jdk15on:1.70")

            library("cloudevents-api", "io.cloudevents:cloudevents-api:2.5.0")
            library("cloudevents-json-jackson", "io.cloudevents:cloudevents-json-jackson:2.5.0")
            library("cloudevents-core", "io.cloudevents:cloudevents-core:2.5.0")
            library("spring-cloud-function-context", "org.springframework.cloud:spring-cloud-function-context:4.0.3")
            library("mailjet-client", "com.mailjet:mailjet-client:5.2.3")

            library("pdfbox", "org.apache.pdfbox:pdfbox:2.0.28")

            library("mockito-kotlin", "org.mockito.kotlin:mockito-kotlin:5.0.0")
            library("mockito-inline", "org.mockito:mockito-inline:5.2.0")

            library("json", "org.json:json:20230618")

            library("commons-io", "commons-io:commons-io:2.12.0")
        }
    }
}
