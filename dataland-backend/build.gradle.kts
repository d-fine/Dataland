plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
    id("com.gorylenko.gradle-git-properties") version "2.4.0"
    id("org.springframework.boot")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]

tasks.withType<org.springdoc.openapi.gradle.plugin.OpenApiGeneratorTask> {
    this.setProperty("outputFileName", "$backendOpenApiJson")
}

val openApiSpec by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts {
    add("openApiSpec", project.file("$buildDir/$backendOpenApiJson")) {
        builtBy("generateOpenApiDocs")
    }
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }
}

jacoco {
    toolVersion = "0.8.7"
    applyTo(tasks.bootRun.get())
}
