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

tasks.register<NpmTask>("buildSharedElements") {
    group = "build"
    description = "Type-checks the shared elements source via vue-tsc"
    dependsOn("npmInstallSharedElements")
    args.set(listOf("run", "typecheck"))
    inputs.files("package.json", "tsconfig.json")
    inputs.dir("src")
    outputs.upToDateWhen { false }
}

// Keep backward-compatible task name
tasks.register("buildSharedFooter") {
    group = "build"
    description = "Alias for buildSharedElements"
    dependsOn("buildSharedElements")
}

// Preempt the node-gradle plugin's task-rule that would otherwise lazily create
// `npm_run_build` and fail because this package has no `build` script.
// The actual typecheck runs via `buildSharedElements` (→ `npm run typecheck`)
// and is wired in by consumers (dataland-frontend, dataland-website).
tasks.register("npm_run_build") {
    group = "build"
    description = "No-op: shared-elements is consumed as a source directory; CI-level npm_run_build fan-out lands here."
}
