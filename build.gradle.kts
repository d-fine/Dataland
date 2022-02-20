allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    group = "org.dataland"
    version = "0.0.1-SNAPSHOT"
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
            jvmTarget = "17"
        }
    }
}

java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    id("org.springframework.boot") version "2.6.2" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10" apply false
    id("org.sonarqube") version "3.3"
    jacoco
}

extra["backendOpenApiJson"] = "backendOpenApi.json"

sonarqube {
    properties {
        property("sonar.projectKey", "d-fine_Dataland")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.jacoco.xmlReportPaths", file("$buildDir/reports/jacoco/test/jacocoTestReport.xml"))
        property("sonar.qualitygate.wait", true)
    }
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    dependsOn(tasks.build)
    sourceDirectories.setFrom(
        subprojects.flatMap { project -> project.sourceSets.asMap.values }
            .flatMap { sourceSet -> sourceSet.allSource }
    )
    classDirectories.setFrom(
        subprojects.flatMap { project -> project.sourceSets.asMap.values }
            .flatMap { sourceSet -> sourceSet.output.classesDirs.flatMap { fileTree(it).files } }
    )
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
    executionData.setFrom(fileTree(projectDir).include("*.exec"))
}
