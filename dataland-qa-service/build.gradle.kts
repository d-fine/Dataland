// dataland-qa-service

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
}

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-message-queue-utils"))
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.amqp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(project(":dataland-keycloak-adapter"))
    implementation(libs.jackson.kotlin)
    implementation(Spring.boot.validation)
    implementation(libs.json)
    implementation(Spring.boot.oauth2ResourceServer)
    implementation(Spring.boot.security)
    implementation(libs.springdoc.openapi.ui)
    implementation(Spring.boot.data.jpa)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.flyway)
    implementation(libs.flyway.core)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    testImplementation(Spring.boot.test)
    testImplementation(Spring.rabbitTest)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.mockito.kotlin)
    kapt(Spring.boot.configurationProcessor)
}

openApi {
    apiDocsUrl.set("http://localhost:8486/qa/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8486"))
    }
    outputFileName.set("$projectDir/qaServiceOpenApi.json")
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
            "serializationLibrary" to "jackson",
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateSpecificationClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the specification service."
    group = "clients"
    val specificationServicePackage = "org.dataland.datalandspecificationservice.openApiClient"
    input = project.file("${project.rootDir}/dataland-specification-service/specificationServiceOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/specification-service")
            .get()
            .toString(),
    )
    packageName.set(specificationServicePackage)
    modelPackage.set("$specificationServicePackage.model")
    apiPackage.set("$specificationServicePackage.api")
    generatorName.set("kotlin")

    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "serializationLibrary" to "jackson",
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateClients") {
    group = "clients"
    dependsOn("generateBackendClient")
    dependsOn("generateSpecificationClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/backend/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/specification-service/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(":dataland-message-queue-utils:assemble")
}
