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

dependencies {
    implementation(libs.junit.jupiter)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateBackendClient", "generateApiKeyManagerClient", "getTestData")
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
            "removeEnumValuePrefix" to false
        )
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
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
            "useTags" to "true"
        )
    )
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("$buildDir/clients/backend/src/main/kotlin")
    main.kotlin.srcDir("$buildDir/clients/api-key-manager/src/main/kotlin")
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
