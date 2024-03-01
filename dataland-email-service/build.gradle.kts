// dataland-community-manager

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
    kotlin("jvm")
    kotlin("plugin.spring")
    jacoco
    id("com.gorylenko.gradle-git-properties")
    id("org.springframework.boot")
    kotlin("kapt")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    // toDo erste line checken, ob diese im späteren Verlauf gebraucht wird
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-message-queue-utils"))
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    // toDo Healthcheck
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    // implementation(Spring.boot.validation)
    implementation(Spring.boot.amqp)
    kapt(Spring.boot.configurationProcessor)
    // implementation(Spring.boot.security)
    testImplementation(Spring.boot.test)
    testImplementation(Testing.mockito.core)
    // testImplementation(Spring.security.spring_security_test)
    implementation(libs.mailjet.client)
    implementation(libs.jackson.kotlin)
    implementation(libs.freemarker)
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
