// dataland-sharedElements/build.gradle.kts
import com.github.gradle.node.npm.task.NpmTask

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
