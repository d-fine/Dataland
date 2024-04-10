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
    version.set("20.11.1")
}

val backendOpenApiFile = "${project.rootDir}/dataland-backend/backendOpenApi.json"
val backendClientOutputDir = "${layout.buildDirectory}/clients/backend"

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.withType<NpmTask> {
    dependsOn("generateClients")
}

tasks.register("generateClients") {
    dependsOn("generateBackendClient")
    dependsOn("generateApiKeyManagerClient")
    dependsOn("generateDocumentManagerClient")
    dependsOn("generateQaServiceClient")
    dependsOn("generateCommunityManagerClient")
}

tasks.register("generateBackendClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.backend"
    input = project.file(backendOpenApiFile).path
    outputDir.set(backendClientOutputDir)
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

tasks.register("generateApiKeyManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.apikeymanager"
    input = project.file("${project.rootDir}/dataland-api-key-manager/apiKeyManagerOpenApi.json").path
    outputDir.set("${layout.buildDirectory}/clients/apikeymanager")
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

tasks.register("generateDocumentManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.documentmanager"
    input = project.file("${project.rootDir}/dataland-document-manager/documentManagerOpenApi.json").path
    outputDir.set("${layout.buildDirectory}/clients/documentmanager")
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}
tasks.register("generateQaServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.qaservice"
    input = project.file("${project.rootDir}/dataland-qa-service/qaServiceOpenApi.json").path
    outputDir.set("${layout.buildDirectory}/clients/qaservice")
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

tasks.register("generateCommunityManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.communitymanager"
    input = project.file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json").path
    outputDir.set("${layout.buildDirectory}/clients/communitymanager")
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

sourceSets {
    val main by getting
    main.java.srcDir("${layout.buildDirectory}/clients/backend/src/main/kotlin")
    main.java.srcDir("${layout.buildDirectory}/clients/documentmanager/src/main/kotlin")
    main.java.srcDir("${layout.buildDirectory}/clients/qaservice/src/main/kotlin")
    main.java.srcDir("${layout.buildDirectory}/clients/apikeymanager/src/main/kotlin")
    main.java.srcDir("${layout.buildDirectory}/clients/communitymanager/src/main/kotlin")
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
