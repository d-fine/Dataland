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
    },
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
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.bcprov.jdk15on)
}

tasks.bootJar {
    enabled = false
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
