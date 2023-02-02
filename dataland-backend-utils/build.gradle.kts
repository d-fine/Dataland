// dataland-backend-utils

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it) {
                exclude("**/openApiClient/**")
            }.files
        }
    }
)
val jacocoVersion: String by project

plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.jpa")
}

// apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.okhttp)
    implementation(libs.slf4j.api)
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.springframework.security:spring-security-web")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("org.springframework.amqp:spring-rabbit")
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.bcprov.jdk15on)
    implementation("io.cloudevents:cloudevents-api:2.4.1")
    implementation("io.cloudevents:cloudevents-json-jackson:2.4.1")
    implementation("io.cloudevents:cloudevents-core:2.3.0")
    implementation("org.springframework.cloud:spring-cloud-function-context:4.0.0")
    implementation("org.springframework.amqp:spring-amqp")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
}

tasks.bootJar {
    enabled = false
}

jacoco {
    toolVersion = jacocoVersion
    applyTo(tasks.bootRun.get())
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
