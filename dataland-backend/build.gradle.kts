// dataland-backend

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
val jacocoVersion: String by project
val openApiGeneratorTimeOutThresholdInSeconds: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("org.springdoc.openapi-gradle-plugin")
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.jpa")
    kotlin("plugin.serialization")
}
dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(libs.jackson.module.kotlin)
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.data.jpa)
    implementation(Spring.boot.validation)
    implementation(Spring.boot.oauth2ResourceServer)
    implementation(Spring.boot.amqp)
    implementation(Spring.boot.security)
    implementation(project(":dataland-keycloak-adapter"))
    implementation(project(":dataland-message-queue-utils"))
    implementation(libs.flyway)
    implementation(libs.flyway.core)
    implementation(Spring.boot.validation)
    implementation(Spring.boot.web)
    implementation(Spring.boot.data.jpa)
    implementation(libs.json)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    testImplementation(Spring.boot.test)
    testImplementation(Testing.mockito.core)
    testImplementation(Spring.security.spring_security_test)
    kapt(Spring.boot.configurationProcessor)
}

openApi {
    apiDocsUrl.set("http://localhost:8482/api/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8482"))
    }
    outputFileName.set("$projectDir/backendOpenApi.json")
    waitTimeInSeconds.set(openApiGeneratorTimeOutThresholdInSeconds.toInt())
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(
            layout.buildDirectory
                .dir("jacoco/jacoco.exec")
                .get()
                .asFile,
        )
    }
}

jacoco {
    toolVersion = jacocoVersion
}

tasks.register<Copy>("getTestData") {
    description = "Task to copy required testing data."
    group = "verification"
    from("$rootDir/testing/data/CompanyInformationWithEutaxonomyNonFinancialsData.json")
    into(
        layout.buildDirectory
            .dir("resources/test")
            .get()
            .toString(),
    )
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

tasks.register("generateInternalStorageClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the internal storage service."
    group = "clients"
    val internalStorageClientDestinationPackage = "org.dataland.datalandinternalstorage.openApiClient"
    input =
        project
            .file("${project.rootDir}/dataland-internal-storage/internalStorageOpenApi.json")
            .path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/internal-storage")
            .get()
            .toString(),
    )
    packageName.set(internalStorageClientDestinationPackage)
    modelPackage.set("$internalStorageClientDestinationPackage.model")
    apiPackage.set("$internalStorageClientDestinationPackage.api")
    generatorName.set("kotlin")

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
tasks.register("generateExternalStorageClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the external storage service."
    group = "clients"
    val externalStorageClientDestinationPackage = "org.dataland.datalandexternalstorage.openApiClient"
    input =
        project
            .file("${project.rootDir}/dataland-external-storage/externalStorageOpenApi.json")
            .path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/external-storage")
            .get()
            .toString(),
    )
    packageName.set(externalStorageClientDestinationPackage)
    modelPackage.set("$externalStorageClientDestinationPackage.model")
    apiPackage.set("$externalStorageClientDestinationPackage.api")
    generatorName.set("kotlin")

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
    val communityManagerClientDestinationPackage = "org.dataland.datalandcommunitymanager.openApiClient"
    input =
        project
            .file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json")
            .path
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
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
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
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
}

tasks.register("generateClients") {
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateInternalStorageClient")
    dependsOn("generateExternalStorageClient")
    dependsOn("generateCommunityManagerClient")
    dependsOn("generateDocumentManagerClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/internal-storage/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/external-storage/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/community-manager/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/document-manager/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**", "**/Activity.kt")
    }
}
