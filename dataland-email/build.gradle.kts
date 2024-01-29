// dataland-email

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
    jacoco
    id("org.springframework.boot")
}

// apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.spring.security.web)
    implementation(project(":dataland-backend-utils"))
    implementation(project(":dataland-keycloak-adapter"))
    implementation(libs.mailjet.client)
}

tasks.bootJar {
    enabled = false
}

jacoco {
    toolVersion = jacocoVersion
}

gitProperties {
    keys = listOf("git.branch", "git.commit.id", "git.commit.time", "git.commit.id.abbrev")
}
