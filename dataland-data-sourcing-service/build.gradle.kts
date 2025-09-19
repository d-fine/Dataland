// dataland-data-sourcing-service

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
    implementation("org.hibernate:hibernate-envers:6.6.26.Final")
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}

openApi {
    outputFileName.set("$projectDir/dataSourcingServiceOpenApi.json")
    apiDocsUrl.set("http://localhost:8483/data-sourcing/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8483"))
    }
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(":dataland-backend-utils:assemble")
    dependsOn(":dataland-keycloak-adapter:assemble")
}
