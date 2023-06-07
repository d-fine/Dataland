// dataland-batch-service

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

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.kotlin)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(libs.commons.io)
    implementation("org.springframework.boot:spring-boot-starter:3.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true",
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
