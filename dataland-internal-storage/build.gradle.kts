// dataland-internal-storage

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
    application
    java
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("org.springdoc.openapi-gradle-plugin")
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.openapi.generator")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly(libs.database.postgres)
    runtimeOnly(libs.database.h2)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
}

openApi {
    outputFileName.set("$projectDir/internalStorageOpenApi.json")
    apiDocsUrl.set("http://localhost:8080/internal-storage/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb"))
    }
}

jacoco {
    toolVersion = jacocoVersion
    applyTo(tasks.bootRun.get())
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
