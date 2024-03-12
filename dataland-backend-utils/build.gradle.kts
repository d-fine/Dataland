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
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.jpa")
}

// apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.okhttp)
    implementation(libs.slf4j.api)
    implementation(libs.spring.security.crypto)
    implementation(libs.spring.security.web)
    implementation(libs.jakarta.servlet.api)
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.bcprov.jdk15on)
    implementation(libs.mailjet.client)
    testImplementation(Spring.boot.test)
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }
}

tasks.bootJar {
    enabled = false
}

jacoco {
    toolVersion = jacocoVersion
}
