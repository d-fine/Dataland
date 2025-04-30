val jacocoSources by extra(sourceSets.asMap.values.flatMap { sourceSet -> sourceSet.allSource })
val jacocoClasses by extra(
    sourceSets.asMap.values.flatMap { sourceSet ->
        sourceSet.output.classesDirs.flatMap {
            fileTree(it) {
                exclude("**/src/**")
            }.files
        }
    },
)

dependencies {
    implementation("org.freemarker:freemarker:2.3.31")
}
