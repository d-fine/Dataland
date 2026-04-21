import org.gradle.api.tasks.Copy

plugins {
    base
}

tasks.register<Copy>("buildSharedFooter") {
    group = "build"
    description = "Prepares the shared Vue footer component"

    from("src/footer") {
        include("TheFooter.vue")
    }

    into(layout.buildDirectory.dir("footer"))
}
