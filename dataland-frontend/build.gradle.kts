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
    version.set("24.9.0")
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

tasks.register<NpmTask>("npmInstallFrontend") {
    group = "build"
    description = "Runs npm install for the frontend"
    args.set(listOf("install"))
    inputs.file("package.json")
    outputs.dir("node_modules/@dataland/shared-elements")
}

tasks.withType<NpmTask> {
    if (name != "npmInstallFrontend") {
        dependsOn("generateClients")
        dependsOn("copyAstroWebsite")
        dependsOn("npmInstallFrontend")
    }
}

tasks.register("copyAstroWebsite") {
    group = "build"
    description = "Copies the Astro website dist into public/ for serving alongside the Vue SPA"
    dependsOn(":dataland-website:npmBuild")

    doLast {
        // Copy everything except index.html directly into public/ so paths like /_astro/ and /static/ work
        copy {
            from("${project.rootDir}/dataland-website/dist") {
                exclude("index.html")
            }
            into("$projectDir/public")
        }
        // Copy index.html as astro-index.html to avoid shadowing the Vue SPA entry point
        copy {
            from("${project.rootDir}/dataland-website/dist/index.html")
            into("$projectDir/public")
            rename("index.html", "astro-index.html")
        }
    }
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
    dependsOn("generateUserServiceClient")
    dependsOn("generateDataSourcingServiceClient")
    dependsOn("generateAccountingServiceClient")
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

tasks.register("generateUserServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the user service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.userservice"
    input = project.file("${project.rootDir}/dataland-user-service/userServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/userservice")
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

tasks.register("generateDataSourcingServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the data sourcing service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.datasourcingservice"
    input = project.file("${project.rootDir}/dataland-data-sourcing-service/dataSourcingServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/datasourcingservice")
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

tasks.register("generateAccountingServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the accounting service."
    group = "clients"
    val destinationPackage = "org.dataland.datalandfrontend.openApiClient.accountingservice"
    input = project.file("${project.rootDir}/dataland-accounting-service/accountingServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/accountingservice")
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
    main.java.srcDir(layout.buildDirectory.dir("clients/userservice/src/main/kotlin"))
    main.java.srcDir(layout.buildDirectory.dir("clients/datasourcingservice/src/main/kotlin"))
    main.java.srcDir(layout.buildDirectory.dir("clients/accountingservice/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
