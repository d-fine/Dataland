// main

val jacocoVersion: String by project
allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "GitHubPackagesSkyminderClient"
            url = uri("https://maven.pkg.github.com/d-fine/DatalandSkyminderClient")
            credentials {
                username = System.getenv("DATALAND_SKYMINDERCLIENT_USER")
                password = System.getenv("DATALAND_SKYMINDERCLIENT_TOKEN")
            }
        }
        maven {
            name = "GitHubPackagesEDCClient"
            url = uri("https://maven.pkg.github.com/d-fine/datalandEDC")
            credentials {
                username = System.getenv("DATALAND_EDC_USER")
                password = System.getenv("DATALAND_EDC_TOKEN")
            }
        }
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.ben-manes.versions")

    group = "org.dataland"
    version = "0.0.1-SNAPSHOT"
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
            jvmTarget = "17"
        }
    }
    sonarqube {
        isSkipProject = true
    }
}

dependencies {
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.20.0")
    detekt("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.21")
}

java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    id("io.gitlab.arturbosch.detekt").version("1.20.0")
    id("org.springframework.boot") version "2.6.7" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.20" apply false
    id("org.sonarqube") version "3.3"
    jacoco
    id("org.springdoc.openapi-gradle-plugin") version "1.3.4" apply false
    id("com.gorylenko.gradle-git-properties") version "2.4.0" apply false
    id("org.openapi.generator") version "5.4.0" apply false
    id("com.github.ben-manes.versions") version "0.42.0"
}

extra["backendOpenApiJson"] = "backendOpenApi.json"

sonarqube {
    properties {
        property("sonar.projectKey", "d-fine_Dataland")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", file("$buildDir/reports/jacoco/test/jacocoTestReport.xml"))
        property("sonar.qualitygate.wait", true)
        property("sonar.javascript.lcov.reportPaths", fileTree("$projectDir/fe-coverage").files)
        property(
            "sonar.coverage.exclusions",
            "**/test/**," +
                "**/tests/**," +
                "**/LocalCorsConfig.kt," +
                "./dataland-frontend/src/main.ts" +
                "./dataland-frontend/src/components/helper/*"
        )
        property(
            "sonar.sources",
            subprojects.flatMap { project -> project.properties["sonarSources"] as Iterable<*> }
        )
    }
}

jacoco {
    toolVersion = jacocoVersion
}

tasks.jacocoTestReport {
    dependsOn(tasks.build)
    sourceDirectories.setFrom(
        subprojects.flatMap { project -> project.properties["jacocoSources"] as Iterable<*> }
    )
    classDirectories.setFrom(
        subprojects.flatMap { project -> project.properties["jacocoClasses"] as Iterable<*> }
    )
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
    executionData.setFrom(fileTree(projectDir).include("*.exec"))
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/config/detekt.yml")
    baseline = file("$projectDir/config/baseline.xml")
    val detektFileTree = fileTree("$projectDir")
    detektFileTree.exclude("**/build/**").exclude("**/node_modules/**").exclude(".gradle")
    source = files(detektFileTree)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
    jvmTarget = java.sourceCompatibility.toString()
}
tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = java.sourceCompatibility.toString()
}
