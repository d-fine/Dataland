// dataland-qa-service

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
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-message-queue-utils"))
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.amqp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(project(":dataland-keycloak-adapter"))
    implementation(Spring.boot.validation)
    implementation(libs.json)
    implementation(Spring.boot.oauth2ResourceServer)
    implementation(Spring.boot.security)
    implementation(libs.springdoc.openapi.ui)
    implementation(Spring.boot.data.jpa)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    testImplementation(Spring.boot.test)
    testImplementation(Spring.rabbitTest)
    kapt(Spring.boot.configurationProcessor)
}

openApi {
    apiDocsUrl.set("http://localhost:8486/qa/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8486"))
    }
    outputFileName.set("$projectDir/qaServiceOpenApi.json")
    waitTimeInSeconds.set(openApiGeneratorTimeOutThresholdInSeconds.toInt())
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("${layout.buildDirectory}/jacoco/jacoco.exec"))
    }
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
