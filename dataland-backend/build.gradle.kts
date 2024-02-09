// dataland-backend

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
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-email"))
    testImplementation(project(":dataland-email").dependencyProject.sourceSets.test.get().output)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.data.jpa)
    implementation(Spring.boot.validation)
    implementation(Spring.boot.oauth2ResourceServer)
    implementation(Spring.boot.amqp)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    kapt(Spring.boot.configurationProcessor)
    implementation(Spring.boot.security)
    testImplementation(Spring.boot.test)
    testImplementation(Testing.mockito.core)
    testImplementation(Spring.security.spring_security_test)
    implementation(project(":dataland-keycloak-adapter"))
    implementation(libs.mailjet.client)
    implementation(project(":dataland-message-queue-utils"))
    implementation(libs.flyway)
    implementation(libs.flyway.core)
    implementation("org.springframework.boot:spring-boot-starter-validation:3.1.5")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

openApi {
    apiDocsUrl.set("http://localhost:8482/api/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8482"))
    }
    outputFileName.set("$projectDir/backendOpenApi.json")
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

tasks.register<Copy>("getTestData") {
    from("$rootDir/testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
    into("$buildDir/resources/test")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

tasks.register("generateInternalStorageClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val internalStorageClientDestinationPackage = "org.dataland.datalandinternalstorage.openApiClient"
    input = project.file("${project.rootDir}/dataland-internal-storage/internalStorageOpenApi.json")
        .path
    outputDir.set("$buildDir/clients/internal-storage")
    packageName.set(internalStorageClientDestinationPackage)
    modelPackage.set("$internalStorageClientDestinationPackage.model")
    apiPackage.set("$internalStorageClientDestinationPackage.api")
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
    dependsOn("generateInternalStorageClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("$buildDir/clients/internal-storage/src/main/kotlin")
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
