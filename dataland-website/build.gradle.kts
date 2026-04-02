import com.github.gradle.node.npm.task.NpmTask

val sonarSources by extra(emptyList<File>())
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node")
}

node {
    download.set(true)
    version.set("24.9.0")
}

tasks.register<NpmTask>("npmBuild") {
    description = "Builds the Astro static website."
    group = "build"
    args.set(listOf("run", "build"))
    dependsOn("npmInstall")
    inputs.dir("src")
    inputs.dir("public")
    inputs.file("astro.config.mjs")
    inputs.file("package.json")
    inputs.file("package-lock.json")
    inputs.file("tsconfig.json")
    outputs.dir("dist")
    // Always rerun: the Gradle cache is shared across CI runs via actions/setup-java,
    // which would otherwise skip this task even when source files have changed.
    outputs.upToDateWhen { false }
}

tasks.register<NpmTask>("npmCheck") {
    description = "Runs Astro type checking."
    group = "verification"
    args.set(listOf("run", "check"))
    dependsOn("npmInstall")
}
