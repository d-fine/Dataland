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
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("org.springdoc.openapi-gradle-plugin")
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.jpa")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
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
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly(libs.database.postgres)
    runtimeOnly(libs.database.h2)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    //implementation(project(":dataland-backend-utils"))
    implementation("io.cloudevents:cloudevents-api:2.4.1")
    implementation("io.cloudevents:cloudevents-json-jackson:2.4.1")
    implementation("io.cloudevents:cloudevents-core:2.3.0")
    implementation("org.springframework.cloud:spring-cloud-function-context:4.0.0")
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
