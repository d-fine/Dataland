// dataland-csvconverter

val sonarSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoSources by extra(sonarSources)
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it).files
        }
    }
)

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
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation(project(":dataland-backend"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Copy>("getTestData") {
    from("$rootDir/testing/data")
    into("$projectDir/src/test/resources")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
