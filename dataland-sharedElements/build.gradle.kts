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

tasks.register<NpmTask>("packSharedElements") {
    group = "build"
    description = "Packs the shared elements into a tarball for consumption by frontend and website"
    dependsOn("npmInstallSharedElements")
    val packDestination = layout.buildDirectory.get().asFile
    val finalTarball = layout.buildDirectory.file("dataland-shared-elements.tgz").get().asFile
    doFirst {
        packDestination.mkdirs()
        finalTarball.delete()
    }
    doLast {
        // npm pack always includes the version in the filename — rename to a fixed name
        val versionedTarball = packDestination.listFiles()?.firstOrNull { it.name.startsWith("dataland-shared-elements-") && it.name.endsWith(".tgz") }
        requireNotNull(versionedTarball) { "npm pack did not produce a tarball in ${packDestination.absolutePath}" }
        versionedTarball.renameTo(finalTarball)
    }
    args.set(listOf("pack", "--pack-destination", packDestination.absolutePath))
    inputs.files("package.json", "tsconfig.json")
    inputs.dir("src")
    outputs.file(finalTarball)
}

// Keep backward-compatible task name
tasks.register("buildSharedFooter") {
    group = "build"
    description = "Alias for packSharedElements"
    dependsOn("packSharedElements")
}
