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
    id("org.openapi.generator")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.okhttp)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    //implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //runtimeOnly(libs.database.postgres)
    //runtimeOnly(libs.database.h2)
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.bcprov.jdk15on)

    // implementation(libs.springdoc.openapi.ui)
    // implementation(libs.junit.jupiter)
    // compileOnly(libs.jakarta.annotation.api)
}

val openApiSpecConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

tasks.register<Copy>("getOpenApiSpec") {
    from(openApiSpecConfig)
    into("$buildDir")
}

val taskName = "generateEdcServer"
val serverOutputDir = "$buildDir/Server/edc"
val apiSpecLocation = "$buildDir/OpenApiSpec.json"
val destinationPackage = "org.dataland.edcDummyServer.openApiServer"

//tasks.register(taskName, org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
//    input = apiSpecLocation
//    outputDir.set(serverOutputDir)
//    modelPackage.set("$destinationPackage.model")
//    apiPackage.set("$destinationPackage.api")
//    packageName.set(destinationPackage)
//    generatorName.set("kotlin-spring")
//    dependsOn("getOpenApiSpec")
//    configOptions.set(
//        mapOf(
//            "dateLibrary" to "java17",
//            "interfaceOnly" to "true",
//            "useTags" to "true"
//        )
//    )
//}

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    dependsOn(taskName)
//}

sourceSets {
    val main by getting
    main.java.srcDir("$serverOutputDir/src/main/kotlin")
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
