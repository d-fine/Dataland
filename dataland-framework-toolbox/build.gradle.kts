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
    jacoco
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
}

tasks.test {
    useJUnitPlatform()

    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("${layout.buildDirectory.get()}/jacoco/jacoco.exec"))
    }
}

tasks.register("integrationTest", JavaExec::class) {
    classpath = sourceSets["test"].runtimeClasspath
    mainClass = "org.dataland.frameworktoolbox.integration.IntegrationTestMainKt"
}

jacoco {
    toolVersion = jacocoVersion
    this.applyTo(tasks.named<JavaExec>("integrationTest").get())
}

dependencies {
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j2.impl)
    implementation(libs.spring.context)
    implementation(libs.spring.test)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.commons.text)
    implementation(libs.slf4j.api)
    implementation("org.gradle:gradle-tooling-api:7.1.1")
    implementation(libs.freemarker)
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}
