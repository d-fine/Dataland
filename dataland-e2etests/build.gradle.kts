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

java.sourceCompatibility = JavaVersion.VERSION_17

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
    val backendClientDestinationPackage = "org.dataland.datalandbackend.openApiClient"
    input = project.file("${project.rootDir}/dataland-backend/backendOpenApi.json").path
    outputDir.set("$buildDir/clients/backend")
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
            "dateLibrary" to "java17",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateQaServiceClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val qaServiceClientDestinationPackage = "org.dataland.datalandqaservice.openApiClient"
    input = project.file("${project.rootDir}/dataland-qa-service/qaServiceOpenApi.json").path
    outputDir.set("$buildDir/clients/qa-service")
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
            "dateLibrary" to "java17",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateApiKeyManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val apiKeyManagerClientDestinationPackage = "org.dataland.datalandapikeymanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-api-key-manager/apiKeyManagerOpenApi.json").path
    outputDir.set("$buildDir/clients/api-key-manager")
    packageName.set(apiKeyManagerClientDestinationPackage)
    modelPackage.set("$apiKeyManagerClientDestinationPackage.model")
    apiPackage.set("$apiKeyManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateDocumentManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val documentManagerClientDestinationPackage = "org.dataland.documentmanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-document-manager/documentManagerOpenApi.json").path
    outputDir.set("$buildDir/clients/document-manager")
    packageName.set(documentManagerClientDestinationPackage)
    modelPackage.set("$documentManagerClientDestinationPackage.model")
    apiPackage.set("$documentManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateCommunityManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val communityManagerClientDestinationPackage = "org.dataland.communitymanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json").path
    outputDir.set("$buildDir/clients/community-manager")
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
            "dateLibrary" to "java17",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateClients") {
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
    main.kotlin.srcDir("$buildDir/clients/backend/src/main/kotlin")
    main.kotlin.srcDir("$buildDir/clients/api-key-manager/src/main/kotlin")
    main.kotlin.srcDir("$buildDir/clients/document-manager/src/main/kotlin")
    main.kotlin.srcDir("$buildDir/clients/qa-service/src/main/kotlin")
    main.kotlin.srcDir("$buildDir/clients/community-manager/src/main/kotlin")
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
    from("$rootDir/testing/data")
    into("$buildDir/resources/test")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
