plugins {
    id("com.github.node-gradle.node") version "3.2.1"
}

node {
    download.set(true)
    version.set("16.14.0")
}

val sources = fileTree(projectDir)
sources.include("src", "public", "tests")
val sonarSources by extra(sources.files.map { it })
val jacocoSources by extra(emptyList<File>())
val jacocoClasses by extra(emptyList<File>())
