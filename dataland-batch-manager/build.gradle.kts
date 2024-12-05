// dataland-batch-service

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
    id("org.springframework.boot")
    kotlin("kapt")
    id("com.gorylenko.gradle-git-properties")
}

dependencies {
    implementation(project(":dataland-backend-utils"))
    implementation(libs.moshi.kotlin)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.kotlin)
    implementation(libs.okhttp)
    implementation(libs.commons.io)
    implementation(Spring.boot.security)
    implementation(Spring.boot.web)
    testImplementation(Spring.boot.test)
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

tasks.register("generateBackendClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the backend service."
    group = "clients"
    val backendClientDestinationPackage = "org.dataland.datalandbackend.openApiClient"
    input = project.file("${project.rootDir}/dataland-backend/backendOpenApi.json").path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/backend")
            .get()
            .toString(),
    )
    packageName.set(backendClientDestinationPackage)
    modelPackage.set("$backendClientDestinationPackage.model")
    apiPackage.set("$backendClientDestinationPackage.api")
    generatorName.set("kotlin")

    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "dateLibrary" to "java21",
            "useTags" to "true",
        ),
    )
}

tasks.register("generateCommunityManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    description = "Task to generate clients for the community manager service."
    group = "clients"
    val communityManagerClientDestinationPackage = "org.dataland.datalandcommunitymanager.openApiClient"
    input =
        project
            .file("${project.rootDir}/dataland-community-manager/communityManagerOpenApi.json")
            .path
    outputDir.set(
        layout.buildDirectory
            .dir("clients/community-manager")
            .get()
            .toString(),
    )
    packageName.set(communityManagerClientDestinationPackage)
    modelPackage.set("$communityManagerClientDestinationPackage.model")
    apiPackage.set("$communityManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    additionalProperties.set(
        mapOf(
            "removeEnumValuePrefix" to false,
        ),
    )
    configOptions.set(
        mapOf(
            "withInterfaces" to "true",
            "withSeparateModelsAndApi" to "true",
        ),
    )
}

tasks.register("generateClients") {
    description = "Task to generate all required clients for the service."
    group = "clients"
    dependsOn("generateBackendClient")
    dependsOn("generateCommunityManagerClient")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateClients")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateClients")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/backend/src/main/kotlin"))
    main.kotlin.srcDir(layout.buildDirectory.dir("clients/community-manager/src/main/kotlin"))
}

ktlint {
    filter {
        exclude("**/openApiClient/**")
    }
}

tasks.register<Copy>("getTestData") {
    description = "Task to copy required testing data."
    group = "verification"
    from("$rootDir/testing/data")
    into(layout.buildDirectory.dir("resources/test"))
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
