// dataland-api-key-manager

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

java.sourceCompatibility = JavaVersion.VERSION_17

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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(project(":dataland-keycloak-adapter"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

openApi {
    outputFileName.set("$projectDir/apiKeyManagerOpenApi.json")
    apiDocsUrl.set("http://localhost:8483/api-keys/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb", "--server.port=8483"))
    }
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

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
