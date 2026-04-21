// dataland-sharedElements/build.gradle.kts
import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.tasks.Copy

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

tasks.register<Copy>("buildSharedFooter") {
    group = "build"
    description = "Prepares the shared Vue footer component"
    dependsOn("npmInstallSharedElements")

    from("src/footer") {
        include("TheFooter.vue", "index.ts")
    }

    into(layout.buildDirectory.dir("footer"))
}
