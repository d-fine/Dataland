plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node")
}

node {
    download.set(true)
    version.set("18.12.1")
}

tasks.register("buildTheme") {
    dependsOn("npm_run_build")
    dependsOn("copyTemplates")
}

tasks.register<Copy>("copyTemplates") {
    from(layout.projectDirectory.dir("templates"))
    include("*/**")
    into(layout.buildDirectory.dir("dist"))
}
