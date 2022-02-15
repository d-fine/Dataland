
plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.openapi.generator") version "5.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.sonarqube") version "3.3"
}

group = "org.dataland"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.5")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.5")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.5")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    //implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    testImplementation("io.rest-assured:kotlin-extensions:4.3.0")

}

tasks.withType<Test> {
    useJUnitPlatform()
}

//Build client API code

val destinationPackage = "org.dataland.datalandbackend"
val backendApiJson = "backendOpenApi.json"

data class ClientConfig(
    val taskName: String, val outputDir: String, val apiSpecLocation: String
    )

val backendClientConfig = ClientConfig(
        taskName = "generateBackendClient",
        outputDir = "$buildDir/Clients/backend",
        apiSpecLocation = "$projectDir/src/main/resources/$backendApiJson"
    )

tasks.register(backendClientConfig.taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(backendClientConfig.apiSpecLocation).path
    outputDir.set(backendClientConfig.outputDir)
    modelPackage.set("$destinationPackage.client.model")
    apiPackage.set("$destinationPackage.client.api")
    packageName.set(destinationPackage)
    generatorName.set("kotlin")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
}

sourceSets {
    val main by getting
    main.java.srcDir("$buildDir/Clients/backend/src/main/kotlin")
}