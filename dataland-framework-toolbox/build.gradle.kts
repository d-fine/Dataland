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

tasks.register("integrationTest", JavaExec::class) {
    description = "Task to execute the integration tests."
    group = "verification"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass = "org.dataland.frameworktoolbox.integration.IntegrationTestMainKt"
    workingDir = rootDir
}

tasks.register("runCoverage", JavaExec::class) {
    doNotTrackState("Application should always run.")
    description = "Execute the main class with jacoco coverage reporting."
    group = "Verification"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.dataland.frameworktoolbox.MainKt"
    workingDir = rootDir
}

tasks.register("runCreateFrameworkList", JavaExec::class) {
    doNotTrackState("Not worth caching.")
    description = "Create a list of all frameworks and store it in 'framework-list.json'"
    group = "Verification"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "org.dataland.frameworktoolbox.MainKt"
    val outputFilePath =
        layout.buildDirectory
            .file("framework-list.json")
            .get()
            .asFile.path
    args = listOf("list", outputFilePath)
    workingDir = rootDir
}

jacoco {
    toolVersion = jacocoVersion
    this.applyTo(tasks.named<JavaExec>("integrationTest").get())
    this.applyTo(tasks.named<JavaExec>("runCoverage").get())
    this.applyTo(tasks.named<JavaExec>("runCreateFrameworkList").get())
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
    implementation(libs.gradle.tooling)
    implementation(libs.freemarker)
    implementation(project(":dataland-specification-lib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}
