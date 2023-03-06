// dataland-message-queue-utils

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
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.jpa")
}

dependencies {
    implementation("org.springframework.amqp:spring-rabbit")
    implementation("io.cloudevents:cloudevents-api:2.4.2")
    implementation("io.cloudevents:cloudevents-json-jackson:2.4.2")
    implementation("io.cloudevents:cloudevents-core:2.4.2")
    implementation("org.springframework.cloud:spring-cloud-function-context:4.0.1")
    implementation("org.springframework.amqp:spring-amqp")
}

tasks.bootJar {
    enabled = false
}

jacoco {
    toolVersion = jacocoVersion
}
