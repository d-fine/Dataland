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
val backendClientOutputDir =
    layout.buildDirectory
        .dir("clients/backend")
        .get()
        .toString()

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.withType<NpmTask> {
    dependsOn("generateClients")
}

tasks.register("generateClients") {
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateBackendClient")
    dependsOn("generateApiKeyManagerClient")
    dependsOn("generateDocumentManagerClient")
    dependsOn("generateQaServiceClient")
    dependsOn("generateCommunityManagerClient")
    dependsOn("generateEmailServiceClient")
}

tasks.register("generateBackendClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the backend service."
    group = "clients"
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
    description = "Task to generate clients for the API-key manager service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.apikeymanager"
    input = project.file("${project.rootDir}/dataland-api-key-manager/apiKeyManagerOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/apikeymanager")
            .get()
            .toString(),
    )
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
    description = "Task to generate clients for the document manager service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.documentmanager"
    input = project.file("${project.rootDir}/dataland-document-manager/documentManagerOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/documentmanager")
            .get()
            .toString(),
    )
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
    description = "Task to generate clients for the QA service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.qaservice"
    input = project.file("${project.rootDir}/dataland-qa-service/qaServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/qaservice")
            .get()
            .toString(),
    )
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
    description = "Task to generate clients for the community manager service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.communitymanager"
    input = project.file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/communitymanager")
            .get()
            .toString(),
    )
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

tasks.register("generateEmailServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the email service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.emailservice"
    input = project.file("${project.rootDir}/dataland-email-service/emailServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/emailservice")
            .get()
            .toString(),
    )
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("typescript-axios")
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

sourceSets {
    val main by getting
    main.java.srcDir(layout.buildDirectory.dir("clients/backend/src/main/kotlin"))
    main.java.srcDir(layout.buildDirectory.dir("clients/documentmanager/src/main/kotlin"))
    main.java.srcDir(layout.buildDirectory.dir("clients/qaservice/src/main/kotlin"))
    main.java.srcDir(layout.buildDirectory.dir("clients/apikeymanager/src/main/kotlin"))
    main.java.srcDir(layout.buildDirectory.dir("clients/communitymanager/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
