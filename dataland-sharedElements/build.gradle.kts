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

tasks.named<NpmTask>("npmInstall") {
    args.set(listOf("ci"))
}

tasks.register<NpmTask>("npmInstallSharedElements") {
    group = "build"
    description = "Installs npm dependencies for dataland-sharedElements"
    args.set(listOf("ci"))
    inputs.file("package.json")
    inputs.file("package-lock.json")
    outputs.dir("node_modules")
}
