@file:Suppress("ktlint:standard:comment-spacing")

import de.fayard.refreshVersions.core.StabilityLevel
rootProject.name = "Dataland"

include(
    "dataland-backend-utils",
    "dataland-backend",
    "dataland-api-key-manager",
    "dataland-internal-storage",
    "dataland-e2etests",
    "dataland-frontend",
    "dataland-keycloak:dataland_theme:login",
    "dataland-keycloak-adapter",
    "dataland-qa-service",
    "dataland-message-queue-utils",
    "dataland-document-manager",
    "dataland-batch-manager",
    "dataland-framework-toolbox",
    "dataland-community-manager",
    "dataland-email-service",
    "dataland-external-storage",
    "dataland-dummy-eurodat-client",
    "dataland-data-exporter",
)

plugins {
    // See https://splitties.github.io/refreshVersions
    id("de.fayard.refreshVersions") version "0.60.5"
}

refreshVersions {
    rejectVersionIf {
        candidate.stabilityLevel != StabilityLevel.Stable
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}
