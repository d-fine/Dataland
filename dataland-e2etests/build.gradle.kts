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
    id("org.openapi.generator") version "5.4.0"
    id("org.springframework.boot")
}

java.sourceCompatibility = JavaVersion.VERSION_17

val backendOpenApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.apache.logging.log4j:log4j:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    backendOpenApiSpecConfig(project(mapOf("path" to ":dataland-backend", "configuration" to "openApiSpec")))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateBackendClient")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]
val taskName = "generateBackendClient"
val clientOutputDir = "$buildDir/Clients/backend"
val apiSpecLocation = "$buildDir/$backendOpenApiJson"
val destinationPackage = "org.dataland.datalandbackend.openApiClient"

tasks.register<Copy>("getBackendOpenApiSpec") {
    from(backendOpenApiSpecConfig)
    into("$buildDir")
}

tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    input = project.file(apiSpecLocation).path
    outputDir.set(clientOutputDir)
    modelPackage.set("$destinationPackage.model")
    apiPackage.set("$destinationPackage.api")
    packageName.set(destinationPackage)
    generatorName.set("kotlin")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
    dependsOn("getBackendOpenApiSpec")
}

sourceSets {
    val main by getting
    main.java.srcDir("$clientOutputDir/src/main/kotlin")
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
    into("$projectDir/src/test/resources")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
