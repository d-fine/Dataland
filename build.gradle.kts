// main

val jacocoVersion: String by project
val ktlintVersion: String by project
val githubUser: String by project
val githubToken: String by project

val jvmVersion = JavaVersion.VERSION_21

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
    alias(libs.plugins.com.github.jk1.dependency.license.report)
    alias(libs.plugins.io.gitlab.arturbosch.detekt)
    alias(libs.plugins.com.github.node.gradle.node) apply false
    alias(libs.plugins.org.springframework.boot) apply false
    alias(libs.plugins.org.jlleitschuh.gradle.ktlint)
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.spring) apply false
    alias(libs.plugins.org.sonarqube)
    jacoco
    alias(libs.plugins.org.springdoc.openapi.gradle.plugin) apply false
    alias(libs.plugins.com.gorylenko.gradle.git.properties) apply false
    alias(libs.plugins.org.openapi.generator) apply false
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.jpa) apply false
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization) apply false
}

sonar {
    properties {
        property("sonar.projectKey", "d-fine_Dataland")
        property("sonar.organization", "d-fine")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.dir("reports/jacoco/test/jacocoTestReport.xml").get().asFile,
        )
        property("sonar.qualitygate.wait", true)
        property("sonar.javascript.lcov.reportPaths", fileTree("$projectDir/fe-coverage").files)
        property("sonar.python.coverage.reportPaths", fileTree("$projectDir/python-coverage").files)
        property(
            "sonar.coverage.exclusions",
            "**/test/**," +
                "**/tests/**," +
                "**/LocalCorsConfig.kt," +
                "dataland-frontend/src/main.ts," +
                "dataland-frontend/src/frameworks/additional-company-information/ViewConfig.ts",
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

            // frontend
            "dataland-frontend/src/components/forms/parts/elements/derived/NaceCodeTree.ts," +
                "dataland-frontend/src/components/forms/parts/elements/derived/ActivityTree.ts," +
                "dataland-frontend/tests/e2e/fixtures/frameworks/eutaxonomy-non-financials/" +
                "EutaxonomyNonFinancialsDataFixtures.ts," +
                // frontend configs
                "dataland-frontend/src/components/resources/frameworkDataSearch/euTaxonomy/configMLDT/" +
                "configForEutaxonomyFinancialsMLDT.ts," +
                "dataland-frontend/src/frameworks/additional-company-information/ViewConfig.ts," +
                "dataland-frontend/src/frameworks/esg-questionnaire/ViewConfig.ts," +
                "dataland-frontend/src/frameworks/lksg/ViewConfig.ts," +
                "dataland-frontend/src/frameworks/sfdr/ViewConfig.ts," +
                "dataland-frontend/src/frameworks/esg-questionnaire/UploadConfig.ts," +
                "dataland-frontend/src/frameworks/heimathafen/UploadConfig.ts," +
                "dataland-frontend/src/frameworks/sfdr/UploadConfig.ts," +
                "dataland-frontend/src/frameworks/vsme/UploadConfig.ts," +
                "dataland-frontend/src/frameworks/lksg/UploadConfig.ts," +
                "dataland-frontend/src/frameworks/custom/EuTaxoNonFinancialsStaticUploadConfig.ts," +
                "dataland-frontend/src/components/resources/frameworkDataSearch/p2p/P2pDataModel.ts," +
                "dataland-frontend/tests/component/utils/LinkExtraction.cy.ts," +
                // backend
                "dataland-backend/src/main/kotlin/db/migration/V1_1__CreateBackendTables.kt," +

                // toolbox
                "dataland-framework-toolbox/src/main/kotlin/org/dataland/frameworktoolbox/intermediate/components" +
                "/Iso2CountryCodesMultiSelectComponent.kt," +
                "dataland-framework-toolbox/src/main/kotlin/org/dataland/frameworktoolbox/frameworks/lksg/custom/" +
                "LksgGeneralViolationAssessmentsComponent.kt," +
                "dataland-framework-toolbox/src/main/kotlin/org/dataland/frameworktoolbox/frameworks/lksg/custom/" +
                "LksgGeneralViolationAssessmentsComponent.kt",
        )
        property(
            "sonar.exclusions",

            // frontend components
            "dataland-frontend/src/components/general/SlideShow.vue," +
                "dataland-frontend/src/components/pages/AboutPage.vue," +
                "dataland-frontend/src/components/generics/TheNewHeader.vue," +
                "dataland-frontend/src/components/generics/TheNewFooter.vue," +
                "dataland-frontend/src/components/resources/aboutPage/**," +
                "dataland-frontend/src/components/resources/newLandingPage/**," +

                // frontend configs
                "dataland-frontend/src/frameworks/heimathafen/ViewConfig.ts," +
                "dataland-frontend/src/frameworks/vsme/ViewConfig.ts," +
                // -> no need to cover these two ViewConfigs because there are no custom fields

                // backend
                "dataland-backend/src/main/kotlin/org/dataland/datalandbackend/frameworks/**," +
                "dataland-backend/src/main/kotlin/org/dataland/datalandbackend/model/enums/eutaxonomy/nonfinancials/" +
                "Activity.kt," +

                // qa-service
                "dataland-qa-service/src/main/kotlin/org/dataland/datalandqaservice/frameworks/**",
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
