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
    dependsOn("copyResources")
    dependsOn("copyMaterialFont")
    dependsOn("copyTemplates")
    dependsOn("copyIbmPlexSans")
}

tasks.register<Copy>("copyResources") {
    from(layout.projectDirectory.dir("resources"))
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

tasks.register<Copy>("copyIbmPlexSans") {
    dependsOn("npm_run_build")
    from(layout.projectDirectory.dir("node_modules/@ibm/plex/IBM-Plex-Sans"))
    include("*/**")
    into(layout.projectDirectory.dir("dist/resources/fonts/IBM-Plex-Sans"))
}

tasks.register<Copy>("copyTemplates") {
    from(layout.projectDirectory.dir("templates"))
    include("*/**")
    into(layout.projectDirectory.dir("dist"))
}
