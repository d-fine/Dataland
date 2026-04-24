import com.github.gradle.node.npm.task.NpmTask

val sources = fileTree(projectDir)
sources.include("src/**", "public/**", "tests/**")
val sonarSources by extra(sources.files.map { it })
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

tasks.withType<NpmTask> {
    // Shared-elements is consumed via a directory reference (file:../dataland-sharedElements),
    // so npm install reads its source directly. buildSharedElements runs the vue-tsc typecheck
    // before any npm activity in the website.
    dependsOn(":dataland-sharedElements:buildSharedElements")
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
