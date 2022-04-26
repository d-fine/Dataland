

// dataland-prepopulator

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
    // kotlin("plugin.spring")
    // id("org.openapi.generator") version "5.4.0"
    id("org.springframework.boot")
}

application {
    mainClass.set("org.dataland.csvconverter.CSVParser")
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.apache.logging.log4j:log4j:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.2")
    // testImplementation("org.springframework.boot:spring-boot-starter-test")
    // implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.13.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation(project(":dataland-backend"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Copy>("getTestData") {
    from("$rootDir/testing/data/DatalandTestDaten.csv")
    into("$projectDir/src/test/resources")
}

tasks.getByName("processTestResources") {
    dependsOn("getTestData")
}
