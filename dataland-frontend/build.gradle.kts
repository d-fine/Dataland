import com.github.gradle.node.npm.task.NpmTask

val sources = fileTree(projectDir)
sources.include("src/**", "public/**", "tests/**")
val sonarSources by extra(sources.files.map { it })
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node")
    id("org.openapi.generator")
}

node {
    download.set(true)
    version.set("18.11.9")
}

val backendOpenApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    backendOpenApiSpecConfig(project(mapOf("path" to ":dataland-backend", "configuration" to "openApiSpec")))
}

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]
val apiClientGenerationTaskName = "generateAPIClientFrontend"
val clientOutputDir = "$buildDir/clients/backend"
val apiSpecLocation = "$clientOutputDir/$backendOpenApiJson"
val destinationPackage = "org.dataland.datalandfrontend.openApiClient"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(apiClientGenerationTaskName)
}

tasks.withType<NpmTask> {
    dependsOn(apiClientGenerationTaskName)
}

tasks.register<Copy>("getBackendOpenApiSpec") {
    from(backendOpenApiSpecConfig)
    into(clientOutputDir)
    filter({ line -> line.replace("http://localhost:8080/api", "/api") })
}

tasks.register(apiClientGenerationTaskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(apiSpecLocation).path
    outputDir.set(clientOutputDir)
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false
        )
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true"
        )
    )
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
