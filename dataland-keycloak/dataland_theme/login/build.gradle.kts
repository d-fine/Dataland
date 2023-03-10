plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node")
}

node {
    download.set(true)
    version.set("19.7.0")
}

tasks.register("buildTheme") {
    dependsOn("npm_run_build")
    dependsOn("copyTemplates")
}

tasks.named<com.github.gradle.node.npm.task.NpmTask>("npm_run_build") {
    inputs.files(fileTree("src"))
    inputs.files(fileTree("resources"))
    inputs.files("package.json")
    inputs.files("package-lock.json")
}

tasks.register<Copy>("copyTemplates") {
    from(layout.projectDirectory.dir("templates"))
    include("*/**")
    into(layout.buildDirectory.dir("dist"))
}
