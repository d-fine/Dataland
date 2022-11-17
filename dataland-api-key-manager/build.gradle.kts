// dataland-api-key-manager

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
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
    // implementation(libs.dataland.edc.client)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(libs.keycloak.spring.boot.starter)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // runtimeOnly(libs.database.postgres)
    // runtimeOnly(libs.database.h2)
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:4.8.0")
}

val apiKeyManagerOpenApiJson = rootProject.extra["apiKeyManagerOpenApiJson"]

openApi {
    outputFileName.set("$apiKeyManagerOpenApiJson")
    apiDocsUrl.set("http://localhost:8080/api-keys/v3/api-docs")
    customBootRun {
        args.set(listOf("--spring.profiles.active=nodb"))
    }
}

val openApiSpec by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}
artifacts {
    add("openApiSpec", project.file("$buildDir/$apiKeyManagerOpenApiJson")) {
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
    toolVersion = jacocoVersion
    applyTo(tasks.bootRun.get())
}

tasks.register<Copy>("getTestData") {
    from("$rootDir/testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
    into("$buildDir/resources")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
