// dataland-external-storage

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
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.data.jpa)
    implementation(Spring.boot.validation)
    implementation(libs.kotlin.reflect)
    implementation(Spring.boot.amqp)
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-message-queue-utils"))
    implementation(Square.okHttp3)
    implementation(libs.json)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
}

openApi {
    apiDocsUrl.set("http://localhost:8488/external-storage/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=isolated", "--server.port=8488"))
    }
    outputFileName.set("$projectDir/externalStorageOpenApi.json")
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

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
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
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

tasks.register("generateEurodatClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the eurodat client."
    group = "clients"
    val eurodatClientDestinationPackage = "org.dataland.datalandeurodatclient.openApiClient"
    input = project.file("${project.rootDir}/dataland-eurodat-client/eurodatClientOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/eurodatclient")
            .get()
            .toString(),
    )
    packageName.set(eurodatClientDestinationPackage)
    modelPackage.set("$eurodatClientDestinationPackage.model")
    apiPackage.set("$eurodatClientDestinationPackage.api")
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
tasks.register("generateClients") {
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateBackendClient")
    dependsOn("generateEurodatClient")
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
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/eurodatclient/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
