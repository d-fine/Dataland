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
    implementation(libs.log4j)
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
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    implementation(libs.kotlin.reflect)
    implementation(Spring.boot.amqp)
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-message-queue-utils"))
    implementation(Square.okHttp3)
    implementation(libs.json)
    testImplementation(Spring.boot.test)
}

openApi {
    apiDocsUrl.set("http://localhost:8484/external-storage/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8484"))
    }
    outputFileName.set("$projectDir/externalStorageOpenApid.json")
    waitTimeInSeconds.set(openApiGeneratorTimeOutThresholdInSeconds.toInt())
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

tasks.register("generateBackendClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val backendClientDestinationPackage = "org.dataland.datalandbackend.openApiClient"
    input = project.file("${project.rootDir}/dataland-backend/backendOpenApi.json")
        .path
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
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

tasks.register("generateClients") {
    dependsOn("generateBackendClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("$buildDir/clients/backend/src/main/kotlin")
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
