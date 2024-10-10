// dataland-dummy-eurodat-client
// TODO check for unnecessary stuff at the end
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
    id("org.springframework.boot")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.jpa")
}

dependencies {
    // TODO At the end: Reduce one by one and check if still works
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.log4j)
    implementation(libs.log4j.api)
    implementation(libs.log4j.to.slf4j)
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
    implementation(libs.slf4j.api)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(Spring.boot.web)
    implementation(Spring.boot.actuator)
    implementation(Spring.boot.data.jpa)
    implementation(Spring.boot.validation)
    runtimeOnly(libs.h2)
    implementation(libs.kotlin.reflect)
    implementation(Square.okHttp3)
    implementation(libs.json)
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

val dummyEurodatClientServerDestinationPackage = "org.dataland.dummyeurodatclient.openApiServer"

tasks.register(
    "generateDummyEurodatClientService",
    org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class,
) {
    description = "Task to generate a Spring web server based on the specification"
    group = "server"
    input = project.file("${project.rootDir}/dataland-eurodat-client/eurodatClientOpenApi.json").path
    outputDir.set("$buildDir/server/dummyeurodatclientservice")
    packageName.set(dummyEurodatClientServerDestinationPackage)
    modelPackage.set("$dummyEurodatClientServerDestinationPackage.model")
    apiPackage.set("$dummyEurodatClientServerDestinationPackage.api")
    generatorName.set("kotlin-spring")
    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "useTags" to "true",
            "useSpringBoot3" to "true",
        ),
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("generateDummyEurodatClientService")
}

tasks.getByName("runKtlintCheckOverMainSourceSet") {
    dependsOn("generateDummyEurodatClientService")
}

sourceSets {
    val main by getting
    main.kotlin.srcDir("$buildDir/server/dummyeurodatclientservice/src")
}

ktlint {
    filter {
        exclude("**/openApiServer/**")
        exclude("**/openapitools/**")
        exclude("**/controller/**") // TODO only during dev!
    }
}
