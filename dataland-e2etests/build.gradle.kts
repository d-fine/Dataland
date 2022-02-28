// dataland-e2etest

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.openapi.generator") version "5.4.0"
}

java.sourceCompatibility = JavaVersion.VERSION_17

val backendOpenApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.apache.logging.log4j:log4j:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.2")
    backendOpenApiSpecConfig(project(mapOf("path" to ":dataland-backend", "configuration" to "openApiSpec")))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
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
    destinationPackage = "org.dataland.datalandbackend.openApiClient"
)

tasks.register<Copy>("getBackendOpenApiSpec") {
    from(backendOpenApiSpecConfig)
    into("$buildDir")
}

tasks.register(clientConfig.taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(clientConfig.apiSpecLocation).path
    outputDir.set(clientConfig.outputDir)
    modelPackage.set("${clientConfig.destinationPackage}.model")
    apiPackage.set("${clientConfig.destinationPackage}.api")
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

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
