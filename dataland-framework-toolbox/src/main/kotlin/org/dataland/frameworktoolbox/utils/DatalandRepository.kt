package org.dataland.frameworktoolbox.utils

import java.nio.file.Path
import kotlin.io.path.div

/**
 * Points to a Dataland Gradle Repository and allows for interaction with the corresponding gradle project
 * @param path the file system path to the repository
 */
class DatalandRepository(
    val path: Path,
) {
    val backendKotlinSrc: Path
        get() = path / "dataland-backend" / "src" / "main" / "kotlin"

    val qaKotlinSrc: Path
        get() = path / "dataland-qa-service" / "src" / "main" / "kotlin"

    val frontendSrc: Path
        get() = path / "dataland-frontend" / "src"

    val gradleInterface = DatalandRepositoryGradleInterface(this)
}
