// dataland-data-exporter

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

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
}

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-keycloak-adapter"))
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(Spring.boot.security)
    kapt(Spring.boot.configurationProcessor)
    testImplementation(Spring.boot.test)
    testImplementation(libs.mockito.kotlin)
}

val backendOpenApiFile = "${project.rootDir}/dataland-backend/backendOpenApi.json"
val backendClientOutputDir =
    layout.buildDirectory
        .dir("clients/backend")
        .get()
        .toString()

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateBackendClient")
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/backend/src/main/kotlin"))
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

tasks.register<Copy>("getTestData") {
    description = "Task to copy required testing data."
    group = "verification"
    from("$rootDir/testing/data/CompanyInformationWithSfdrPreparedFixtures.json")
    into(layout.buildDirectory.dir("resources/test"))
}
tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
