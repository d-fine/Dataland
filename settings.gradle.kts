rootProject.name = "dataland"
include("dataland-backend", "dataland-e2etests", "dataland-frontend", "dataland-csvconverter")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("springdoc-openapi-ui", "org.springdoc:springdoc-openapi-ui:1.6.8")

            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.8.2")
            library("junit-jupiter-engine", "org.junit.jupiter:junit-jupiter-engine:5.8.2")
            library("junit-jupiter-api", "org.junit.jupiter:junit-jupiter-api:5.8.2")

            library("moshi-kotlin", "com.squareup.moshi:moshi-kotlin:1.13.0")
            library("moshi-adapters", "com.squareup.moshi:moshi-adapters:1.13.0")

            library("swagger-jaxrs2-jakarta", "io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.0")
            library("swagger-gradle-plugin", "io.swagger.core.v3:swagger-gradle-plugin:2.2.0")
            library("swagger-annotations", "io.swagger.core.v3:swagger-annotations:2.2.0")

            library("okhttp", "com.squareup.okhttp3:okhttp:4.9.3")
            library("rs-api", "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")

            library("skyminder-client", "org.dataland:skyminder-client:0.1.4")
            library("dataland-edc-client", "org.dataland:dataland-edc-client:0.2.0")

            library("log4j", "org.apache.logging.log4j:log4j:2.17.2")
            library("log4j-api", "org.apache.logging.log4j:log4j-api:2.17.2")
            library("log4j-to-slf4j", "org.apache.logging.log4j:log4j-to-slf4j:2.17.2")

            library("logback-classic", "ch.qos.logback:logback-classic:1.2.11")
            library("logback-core", "ch.qos.logback:logback-core:1.2.11")

            library("slf4j-api", "org.slf4j:slf4j-api:1.7.36")

            library("jackson-dataformat-csv", "com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.13.2")
            library("jackson-databind", "com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
            library("jackson-datatype-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
        }
    }
}
