plugins {
    kotlin("jvm")
    id("com.github.node-gradle.node")
}

node {
    download.set(true)
    version.set("18.6.0")
}

tasks.register("buildTheme") {
    dependsOn("npm_run_build")
    dependsOn("copyResources")
    dependsOn("copyMaterialFont")
    dependsOn("copyTemplates")
}

tasks.register<Copy>("copyResources") {
    from(layout.projectDirectory.dir("resources"))
    include("fonts/*")
    include("img/*")
    into(layout.projectDirectory.dir("dist/resources"))
}

tasks.register<Copy>("copyMaterialFont") {
    dependsOn("npm_run_build")
    from(layout.projectDirectory.dir("node_modules/material-icons/iconfont"))
    include("*.woff")
    include("*.woff2")
    into(layout.projectDirectory.dir("dist/resources/fonts"))
}

tasks.register<Copy>("copyTemplates") {
    from(layout.projectDirectory.dir("templates"))
    include("*")
    into(layout.projectDirectory.dir("dist"))
}