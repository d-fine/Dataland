package org.dataland.frameworktoolbox.integration

import java.io.File
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework

/**
 * The Integration Testing Framework is a framework that contains a representative subset of fields
 * to test the integration of the testing generators.
 */
class IntegrationTestFramework :
  PavedRoadFramework(
    identifier = "integrationTesting",
    label = "Integration Testing",
    explanation = "This framework is used for testing the framework-toolbox",
    frameworkTemplateCsvFile =
      File(
        "./dataland-framework-toolbox/src/test/resources/org/dataland/" +
          "frameworktoolbox/integration/integrationTestFrameworkDefinition.csv"
      ),
    enabledFeatures =
      setOf(
        FrameworkGenerationFeatures.BackendDataModel,
        FrameworkGenerationFeatures.BackendApiController,
        FrameworkGenerationFeatures.ViewPage,
        FrameworkGenerationFeatures.FakeFixtures,
      ),
    order = 0,
  )
