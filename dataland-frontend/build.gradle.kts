plugins {
    id("com.github.node-gradle.node") version "3.2.1"
}

node {
    download.set(true)
    version.set("16.14.0")
}

val sources = fileTree(projectDir)
sources.include("src", "public", "tests")
val sonarSources = sources.files.map { it }
