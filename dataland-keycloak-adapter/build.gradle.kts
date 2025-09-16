// dataland-keycloak-adapter

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
    id("org.springframework.boot")
    kotlin("kapt")
}

dependencies {
    implementation(libs.spring.context)
    implementation(libs.spring.boot)
    implementation(Spring.boot.web)
    implementation(Spring.boot.oauth2ResourceServer)
    implementation(Spring.boot.security)
    kapt(Spring.boot.configurationProcessor)
    implementation(project(":dataland-backend-utils"))
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(Testing.mockito.core)
}

java {
    withSourcesJar()
}

tasks.bootJar {
    enabled = false
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
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateClients") {
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateApiKeyManagerClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.getByName("sourcesJar") {
    dependsOn("generateClients")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/api-key-manager/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
