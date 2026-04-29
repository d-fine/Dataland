// dataland-sharedElements/build.gradle.kts
import com.github.gradle.node.npm.task.NpmTask

val sources = fileTree(projectDir)
sources.include("src/**", "public/**")
val sonarSources by extra(sources.files.map { it })
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())

plugins {
    base
    id("com.github.node-gradle.node")
}

node {
    download.set(true)
    version.set("24.9.0")
}

tasks.register<NpmTask>("npmInstallSharedElements") {
    group = "build"
    description = "Installs npm dependencies for dataland-sharedElements"
    args.set(listOf("install"))
    inputs.file("package.json")
    outputs.dir("node_modules")
}
