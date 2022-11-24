// dataland-keycloak-adapter

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)
val jacocoVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("org.springframework.boot")
    kotlin("kapt")

    id("org.springdoc.openapi-gradle-plugin")
    id("com.gorylenko.gradle-git-properties")
    id("org.jetbrains.kotlin.plugin.jpa")
}

java.sourceCompatibility = JavaVersion.VERSION_17

val apiKeyManagerOpenApiSpecConfig: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}
dependencies {
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // TODO: Reparieren
    // kapt("org.springframework.boot:spring-boot-configuration-processor")
    apiKeyManagerOpenApiSpecConfig(project(
        mapOf("path" to ":dataland-api-key-manager", "configuration" to "openApiSpec")))
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
}

java {
    withSourcesJar()
}

tasks.bootJar {
    enabled = false
}

val openApiClientsOutputDir = "$buildDir/clients"
val apiKeyManagerOpenApiJson = rootProject.extra["apiKeyManagerOpenApiJson"]
val apiKeyManagerClientDestinationPackage = "org.dataland.datalandapikeymanager.openApiClient"

tasks.register("generateApiKeyManagerClient",
    org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file("$buildDir/$apiKeyManagerOpenApiJson").path
    outputDir.set("$openApiClientsOutputDir/api-key-manager")
    packageName.set(apiKeyManagerClientDestinationPackage)
    modelPackage.set("$apiKeyManagerClientDestinationPackage.model")
    apiPackage.set("$apiKeyManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
    dependsOn("getApiKeyManagerOpenApiSpec")
}

tasks.register<Copy>("getApiKeyManagerOpenApiSpec") {
    from(apiKeyManagerOpenApiSpecConfig)
    into("$buildDir")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateApiKeyManagerClient")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("$openApiClientsOutputDir/api-key-manager/src/main/kotlin")
}
