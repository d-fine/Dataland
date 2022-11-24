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
    version.set("18.12.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateAPIClientFrontend")
}

tasks.withType<NpmTask> {
    dependsOn("generateAPIClientFrontend")
}

tasks.register("generateAPIClientFrontend", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient"
    input = project.file("${project.rootDir}/dataland-backend/backendOpenApi.json").path
    outputDir.set("$buildDir/clients/backend")
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
}

sourceSets {
    val main by getting
    main.java.srcDir("$buildDir/clients/backend/src/main/kotlin")
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
