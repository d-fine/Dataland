// dataland-e2etest

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it) {
                exclude("**/openApiClient/**")
            }.files
        }
    },
)

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.openapi.generator")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(libs.junit.jupiter)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(Spring.boot.web)
    testImplementation(Spring.boot.test)
    testImplementation(libs.awaitility)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("generateBackendClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the backend service."
    group = "clients"
    val backendClientDestinationPackage = "org.dataland.datalandbackend.openApiClient"
    input = project.file("${project.rootDir}/dataland-backend/backendOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/backend")
            .get()
            .toString(),
    )
    packageName.set(backendClientDestinationPackage)
    modelPackage.set("$backendClientDestinationPackage.model")
    apiPackage.set("$backendClientDestinationPackage.api")
    generatorName.set("kotlin")

    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateQaServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the QA service."
    group = "clients"
    val qaServiceClientDestinationPackage = "org.dataland.datalandqaservice.openApiClient"
    input = project.file("${project.rootDir}/dataland-qa-service/qaServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/qa-service")
            .get()
            .toString(),
    )
    packageName.set(qaServiceClientDestinationPackage)
    modelPackage.set("$qaServiceClientDestinationPackage.model")
    apiPackage.set("$qaServiceClientDestinationPackage.api")
    generatorName.set("kotlin")
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateApiKeyManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the API-key manager service."
    group = "clients"
    val apiKeyManagerClientDestinationPackage = "org.dataland.datalandapikeymanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-api-key-manager/apiKeyManagerOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/api-key-manager")
            .get()
            .toString(),
    )
    packageName.set(apiKeyManagerClientDestinationPackage)
    modelPackage.set("$apiKeyManagerClientDestinationPackage.model")
    apiPackage.set("$apiKeyManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    configOptions.set(
        mapOf(
            "dateLibrary" to "jav21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateDocumentManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the document manager service."
    group = "clients"
    val documentManagerClientDestinationPackage = "org.dataland.documentmanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-document-manager/documentManagerOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/document-manager")
            .get()
            .toString(),
    )
    packageName.set(documentManagerClientDestinationPackage)
    modelPackage.set("$documentManagerClientDestinationPackage.model")
    apiPackage.set("$documentManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateCommunityManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the community manager service."
    group = "clients"
    val communityManagerClientDestinationPackage = "org.dataland.communitymanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/community-manager")
            .get()
            .toString(),
    )
    packageName.set(communityManagerClientDestinationPackage)
    modelPackage.set("$communityManagerClientDestinationPackage.model")
    apiPackage.set("$communityManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

// TODO add client for email service tests

tasks.register("generateClients") {
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateBackendClient")
    dependsOn("generateQaServiceClient")
    dependsOn("generateApiKeyManagerClient")
    dependsOn("generateDocumentManagerClient")
    dependsOn("generateCommunityManagerClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients", "getTestData")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/backend/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/api-key-manager/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/document-manager/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/qa-service/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/community-manager/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}

tasks.bootJar {
    enabled = false
}

tasks.register<Copy>("getTestData") {
    description = "Task to copy required testing data."
    group = "verification"
    from("$rootDir/testing/data")
    into(layout.buildDirectory.dir("resources/test"))
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
