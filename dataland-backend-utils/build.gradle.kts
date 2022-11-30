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
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.guava:guava:31.0.1-jre")
    api("org.apache.commons:commons-math3:3.6.1")

    implementation(libs.springdoc.openapi.ui)
    implementation(libs.okhttp)
    implementation(libs.slf4j.api)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.bcprov.jdk15on)
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
