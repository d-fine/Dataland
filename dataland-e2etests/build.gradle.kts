// dataland-e2etest

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.openapi.generator")
    id("org.springframework.boot")
}

java.sourceCompatibility = JavaVersion.VERSION_17

val backendOpenApiSpecConfig: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val apiKeyManagerOpenApiSpecConfig: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(libs.junit.jupiter)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation("org.springframework.boot:spring-boot-starter-web")
    backendOpenApiSpecConfig(project(mapOf("path" to ":dataland-backend", "configuration" to "openApiSpec")))
    apiKeyManagerOpenApiSpecConfig(project(mapOf("path" to ":dataland-api-key-manager", "configuration" to "openApiSpec")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateBackendClient", "getTestData")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val openApiClientsOutputDir = "$buildDir/clients"

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]
val apiKeyManagerOpenApiJson = rootProject.extra["apiKeyManagerOpenApiJson"]

val backendClientDestinationPackage = "org.dataland.datalandbackend.openApiClient"
val apiKeyManagerClientDestinationPackage = "org.dataland.datalandapikeymanager.openApiClient"

tasks.register<Copy>("getBackendOpenApiSpec") {
    from(backendOpenApiSpecConfig)
    into("$buildDir")
    dependsOn("getApiKeyManagerOpenApiSpec")
    dependsOn("generateApiKeyManagerClient")
}

tasks.register<Copy>("getApiKeyManagerOpenApiSpec") {
    from(apiKeyManagerOpenApiSpecConfig)
    into("$buildDir")
}

tasks.register("generateBackendClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file("$buildDir/$backendOpenApiJson").path
    outputDir.set("$openApiClientsOutputDir/backend")
    packageName.set(backendClientDestinationPackage)
    modelPackage.set("$backendClientDestinationPackage.model")
    apiPackage.set("$backendClientDestinationPackage.api")
    generatorName.set("kotlin")

    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false
        )
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
    dependsOn("getBackendOpenApiSpec")
}

tasks.register("generateApiKeyManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
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

sourceSets {
    val main by getting
    main.kotlin.srcDir("$openApiClientsOutputDir/backend/src/main/kotlin")
    main.kotlin.srcDir("$openApiClientsOutputDir/api-key-manager/src/main/kotlin")
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
    into("$buildDir/resources")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
