import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.openapi.generator") version "5.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

group = "org.dataland"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

val backendOpenApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.5")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.5")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.5")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    testImplementation("io.rest-assured:kotlin-extensions:4.3.0")
    backendOpenApiSpecConfig(project(mapOf("path" to ":dataland-backend", "configuration" to "openApiSpec")))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
    }
    dependsOn("generateBackendClient")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]

data class ClientConfig(
    val taskName: String,
    val outputDir: String,
    val apiSpecLocation: String,
    val destinationPackage: String
)

val clientConfig = ClientConfig(
    taskName = "generateBackendClient",
    outputDir = "$buildDir/Clients/backend",
    apiSpecLocation = "$buildDir/$backendOpenApiJson",
    destinationPackage = "org.dataland.datalandbackend"
)

tasks.register<Copy>("getBackendOpenApiSpec") {
    from(backendOpenApiSpecConfig)
    into("$buildDir")
}

tasks.register(clientConfig.taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(clientConfig.apiSpecLocation).path
    outputDir.set(clientConfig.outputDir)
    modelPackage.set("${clientConfig.destinationPackage}.client.model")
    apiPackage.set("${clientConfig.destinationPackage}.client.api")
    packageName.set(clientConfig.destinationPackage)
    generatorName.set("kotlin")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
    dependsOn("getBackendOpenApiSpec")
}

sourceSets {
    val main by getting
    main.java.srcDir("$buildDir/Clients/backend/src/main/kotlin")
}
