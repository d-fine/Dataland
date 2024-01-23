// main

val jacocoVersion: String by project
val ktlintVersion: String by project
val githubUser: String by project
val githubToken: String by project

val jvmVersion = JavaVersion.VERSION_17

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.gradle.org/gradle/libs-releases") }
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "com.github.jk1.dependency-license-report")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "org.dataland"
    version = "0.0.1-SNAPSHOT"
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
            jvmTarget = jvmVersion.majorVersion
        }
    }
    sonar {
        isSkipProject = true
    }
    ktlint {
        version.set(ktlintVersion)
    }
    kotlin {
        jvmToolchain(jvmVersion.majorVersion.toInt())
    }
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(jvmVersion.majorVersion.toInt())
        }
    }
}

tasks.dependencyUpdates.configure {
    gradleReleaseChannel = "current"
}

dependencies {
    detekt(libs.detekt.cli)
    detekt(libs.kotlin.compiler.embeddable)
}

java.sourceCompatibility = jvmVersion

plugins {
    id("com.github.jk1.dependency-license-report")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.node-gradle.node") apply false
    id("org.springframework.boot") apply false
    id("org.jlleitschuh.gradle.ktlint")
    kotlin("jvm")
    kotlin("plugin.spring") apply false
    id("org.sonarqube") version "4.3.0.3225"
    jacoco
    id("org.springdoc.openapi-gradle-plugin") apply false
    id("com.gorylenko.gradle-git-properties") apply false
    id("org.openapi.generator") apply false
    id("com.github.ben-manes.versions")
    id("org.jetbrains.kotlin.plugin.jpa") apply false
    kotlin("plugin.serialization") apply false
}

sonar {
    properties {
        property("sonar.projectKey", "d-fine_Dataland")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", file("$buildDir/reports/jacoco/test/jacocoTestReport.xml"))
        property("sonar.qualitygate.wait", true)
        property("sonar.javascript.lcov.reportPaths", fileTree("$projectDir/fe-coverage").files)
        property("sonar.python.coverage.reportPaths", fileTree("$projectDir/python-coverage").files)
        property(
            "sonar.coverage.exclusions",
            "**/test/**," +
                "**/tests/**," +
                "**/LocalCorsConfig.kt," +
                "dataland-frontend/src/main.ts," +
                "dataland-framework-toolbox/src/main/kotlin/org/dataland/frameworktoolbox/**",
        )
        property(
            "sonar.sources",
            subprojects.flatMap { project -> project.properties["sonarSources"] as Iterable<*> } +
                fileTree("$projectDir/dataland-automated-qa-service").files,
        )
        property("sonar.verbose", "true")
        property("sonar.scanner.metadataFilePath", "$projectDir/build/reports/report_task.txt")
        property(
            "sonar.cpd.exclusions",
            "dataland-frontend/src/components/forms/parts/elements/derived/NaceCodeTree.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/lksg/LksgDataModel.ts," +
                "dataland-backend/src/main/kotlin/db/migration/V1_1__CreateBackendTables.kt," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/p2p/P2pDataModel.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/sfdr/SfdrDataModel.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/sme/SmeDataModel.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/euTaxonomy/" +
                "EuTaxonomyForNonFinancialsDataModel.ts," +
                "dataland-frontend/src/components/forms/parts/elements/derived/ActivityTree.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/sme/SmeDataModel.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/euTaxonomy/" +
                "EuTaxonomyForNonFinancialsDisplayDataModel.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/euTaxonomy/configMLDT/" +
                "configForEutaxonomyFinancialsMLDT.ts," +
                "dataland-frontend/src/frameworks/esg-questionnaire/ViewConfig.ts," +
                "dataland-frontend/src/frameworks/esg-questionnaire/UploadConfig.ts",
        )
        property(
            "sonar.exclusions",
            "dataland-backend/src/main/kotlin/" +
                "org/dataland/datalandbackend/model/enums/eutaxonomy/nonfinancials/Activity.kt," +
                "dataland-frontend/src/components/general/SlideShow.vue," +
                "dataland-frontend/src/components/resources/newLandingPage/TheQuotes.vue," +
                "dataland-backend/src/main/kotlin/org/dataland/datalandbackend/frameworks/**," +
                "dataland-frontend/tests/e2e/fixtures/frameworks/esg-questionnaire/EsgQuestionnaireDataFixtures.ts" +
                "dataland-frontend/src/components/resources/aboutPage/TheAboutIntro.vue" +
                "dataland-frontend/src/components/resources/aboutPage/TheAboutPrinciples.vue" +
                "dataland-frontend/src/components/pages/AboutPage.vue" +
                "dataland-frontend/src/components/generics/TheNewFooter.vue",
        )
    }
}

jacoco {
    toolVersion = jacocoVersion
}

tasks.jacocoTestReport {
    dependsOn(tasks.build)
    dependsOn(subprojects.flatMap { it.tasks.filter { it.name == "compileKotlin" } })
    sourceDirectories.setFrom(
        subprojects.flatMap { project -> project.properties["jacocoSources"] as Iterable<*> },
    )
    classDirectories.setFrom(
        subprojects.flatMap { project -> project.properties["jacocoClasses"] as Iterable<*> },
    )
    reports {
        xml.required.set(true)
        csv.required.set(false)
    }
    executionData.setFrom(fileTree("$projectDir") { include("**/*.exec") }.files)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt.yml")
    baseline = file("$projectDir/config/baseline.xml")
    val detektFileTree = fileTree("$projectDir")
    detektFileTree.exclude("**/build/**").exclude("**/node_modules/**").exclude(".gradle")
    source.setFrom(detektFileTree)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}

ktlint {
    version.set(ktlintVersion)
}
