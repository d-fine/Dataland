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
    implementation(libs.spring.rabbit)
    implementation(libs.cloudevents.api)
    implementation(libs.cloudevents.json.jackson)
    implementation(libs.cloudevents.core)
    implementation(libs.spring.cloud.function.context)
    implementation(libs.spring.amqp)
    implementation(libs.json)
    implementation(project(":dataland-backend-utils"))
}

tasks.bootJar {
    enabled = false
}

jacoco {
    toolVersion = jacocoVersion
}
