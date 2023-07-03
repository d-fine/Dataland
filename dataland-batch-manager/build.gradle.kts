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
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.kotlin)
    implementation(libs.okhttp)
    implementation(libs.commons.io)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-security")
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

tasks.register<Copy>("getTestData") {
    from("$rootDir/testing/data")
    into("$buildDir/resources/test")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
