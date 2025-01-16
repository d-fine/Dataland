// dataland-specification-service

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
val openApiGeneratorTimeOutThresholdInSeconds: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("org.springdoc.openapi-gradle-plugin")
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
}

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-specification-lib"))
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.validation)
    implementation(libs.jackson.module.kotlin)
    testImplementation(Spring.boot.test)
}

openApi {
    apiDocsUrl.set("http://localhost:8489/specifications/v3/api-docs")
    customBootRun {
        args.set(listOf("--server.port=8489"))
    }
    outputFileName.set("$projectDir/specificationServiceOpenApi.json")
    waitTimeInSeconds.set(openApiGeneratorTimeOutThresholdInSeconds.toInt())
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(
            layout.buildDirectory
                .dir("jacoco/jacoco.exec")
                .get()
                .asFile,
        )
    }
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}

afterEvaluate {
    tasks.getByName("forkedSpringBootRun") {
        dependsOn(":dataland-backend-utils:assemble")
        dependsOn(":dataland-message-queue-utils:assemble")
        dependsOn(":dataland-specification-lib:assemble")
        notCompatibleWithConfigurationCache(
            "See https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/102",
        )
    }

    tasks.getByName("forkedSpringBootStop") {
        notCompatibleWithConfigurationCache(
            "See https://github.com/springdoc/springdoc-openapi-gradle-plugin/issues/102",
        )
    }
}
