
val sources = fileTree(projectDir)
sources.include("src/**", "public/**", "tests/**")
val sonarSources by extra(sources.files.map { it })
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node") version "3.2.1"
    id("org.openapi.generator") version "5.4.0"
}

node {
    download.set(true)
    version.set("16.15.0")
}

val backendOpenApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    backendOpenApiSpecConfig(project(mapOf("path" to ":dataland-backend", "configuration" to "openApiSpec")))
}

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]
val taskName = "generateAPIClientFrontend"
val clientOutputDir = "$buildDir/clients/backend"
val apiSpecLocation = "$clientOutputDir/$backendOpenApiJson"
val destinationPackage = "org.dataland.datalandfrontend.openApiClient"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(taskName)
}

tasks.register<Copy>("getBackendOpenApiSpec") {
    from(backendOpenApiSpecConfig)
    into(clientOutputDir)
    filter({ line -> line.replace("http://localhost:8080/api", "/api") })
}

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(apiSpecLocation).path
    outputDir.set(clientOutputDir)
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    dependsOn("getBackendOpenApiSpec")
}

sourceSets {
    val main by getting
    main.java.srcDir("$clientOutputDir/src/main/kotlin")
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
