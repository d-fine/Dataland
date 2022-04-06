// dataland-backend

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.4"
    id("com.gorylenko.gradle-git-properties") version "2.4.0"
    id("org.springframework.boot")
    kotlin("kapt")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")
    implementation("org.dataland:skyminder-client:0.1.2")
    implementation("org.dataland:dataland-edc-client:0.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.apache.logging.log4j:log4j:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.2")
    implementation("org.reflections:reflections:0.10.2")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val backendOpenApiJson = rootProject.extra["backendOpenApiJson"]

tasks.withType<org.springdoc.openapi.gradle.plugin.OpenApiGeneratorTask> {
    outputFileName.set("$backendOpenApiJson")
    apiDocsUrl.set("http://localhost:8080/api/v3/api-docs")
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
