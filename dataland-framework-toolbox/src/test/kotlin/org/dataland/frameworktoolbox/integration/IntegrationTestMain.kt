package org.dataland.frameworktoolbox.integration

import java.nio.file.Path
import org.dataland.frameworktoolbox.utils.DatalandRepository

/**
 * This test executes the framework toolbox on a demo framework. Executing this test WILL MAKE
 * CHANGES TO YOUR DATALAND REPOSITORY. Therefore, you are advised to not commit any of the files
 * changed by this test or execute it in an isolated environment.
 */
fun main() {
  val datalandProject = DatalandRepository(Path.of("./"))

  IntegrationTestFramework().compileFramework(datalandProject)
}
