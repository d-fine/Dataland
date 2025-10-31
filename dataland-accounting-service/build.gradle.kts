// dataland-accounting-service

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
    implementation(project(":dataland-message-queue-utils"))
    implementation(libs.springdoc.openapi.ui)
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
    implementation(Spring.boot.security)
    implementation(project(":dataland-keycloak-adapter"))
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    kapt(Spring.boot.configurationProcessor)
    testImplementation(Spring.boot.test)
    implementation(libs.flyway)
    implementation(libs.flyway.core)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    testImplementation(libs.mockito.kotlin)
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":dataland-backend-utils", "testArtifacts"))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}

openApi {
    apiDocsUrl.set("http://localhost:8483/accounting/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8483"))
    }
    outputFileName.set("$projectDir/accountingServiceOpenApi.json")
    waitTimeInSeconds.set(openApiGeneratorTimeOutThresholdInSeconds.toInt())
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

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

tasks.register("generateCommunityManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the community manager service."
    group = "clients"
    val communityManagerClientDestinationPackage = "org.dataland.datalandcommunitymanager.openApiClient"
    input =
        project
            .file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json")
            .path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/community-manager")
            .get()
            .toString(),
    )
    packageName.set(communityManagerClientDestinationPackage)
    modelPackage.set("$communityManagerClientDestinationPackage.model")
    apiPackage.set("$communityManagerClientDestinationPackage.api")
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
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateCommunityManagerClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
    dependsOn(":dataland-backend-utils:assemble")
    dependsOn(":dataland-message-queue-utils:assemble")
    dependsOn(":dataland-keycloak-adapter:assemble")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

tasks.getByName("ktlintMainSourceSetCheck") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/community-manager/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}
