// dataland-keycloak-adapter

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
    id("org.springframework.boot")
    kotlin("kapt")

    id("com.gorylenko.gradle-git-properties")
    id("org.jetbrains.kotlin.plugin.jpa")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // TDO: Reparieren
    // kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation(project(":dataland-backend-utils"))
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.okhttp)
}

java {
    withSourcesJar()
}

tasks.bootJar {
    enabled = false
}

tasks.register("generateApiKeyManagerClient", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    val apiKeyManagerClientDestinationPackage = "org.dataland.datalandapikeymanager.openApiClient"
    input = project.file("${project.rootDir}/dataland-api-key-manager/apiKeyManagerOpenApi.json").path
    outputDir.set("$buildDir/clients/api-key-manager")
    packageName.set(apiKeyManagerClientDestinationPackage)
    modelPackage.set("$apiKeyManagerClientDestinationPackage.model")
    apiPackage.set("$apiKeyManagerClientDestinationPackage.api")
    generatorName.set("kotlin")

    configOptions.set(
        mapOf(
            "dateLibrary" to "java17",
            "useTags" to "true"
        )
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateApiKeyManagerClient")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("$buildDir/clients/api-key-manager/src/main/kotlin")
}
