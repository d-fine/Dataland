// dataland-csvconverter

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
    application
    jacoco
    id("org.springframework.boot")
}

application {
    mainClass.set("org.dataland.csvconverter.CsvToJsonConverter")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation(libs.junit.jupiter)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.kotlin)
    implementation(project(":dataland-backend"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Copy>("getTestData") {
    from("$rootDir/testing/data")
    into("$buildDir/resources")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}

jacoco {
    toolVersion = jacocoVersion
}
