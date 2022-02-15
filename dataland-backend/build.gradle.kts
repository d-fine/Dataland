import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springdoc.openapi.gradle.plugin.OpenApiGeneratorTask


plugins {
    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.openapi.generator") version "5.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.sonarqube") version "3.3"
    jacoco
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
}

group = "org.dataland"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.6.5")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.5")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val backendApiJson = "backendOpenApi.json"

tasks.withType<OpenApiGeneratorTask> {
    this.setProperty("outputFileName", backendApiJson)
}

// ADD TASK TO SHARE ARTIFACT WITH E2E TEST PROJECT

tasks.register("generateOpenApiDocsTask", org.springdoc.openapi.gradle.plugin.OpenApiGeneratorTask::class) {
}
/*
val openApiSpec by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}*/
artifacts {
    add("openApiSpec", generateOpenApiDocsTask.outputFile) {
        builtBy(generateOpenApiDocsTask)
    }
}




tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }

    finalizedBy("jacocoTestReport")
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "d-fine_Dataland")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.jacoco.reportPaths", file("$buildDir/jacoco/jacoco.exec"))
        property("sonar.qualitygate.wait", true)
    }
}
