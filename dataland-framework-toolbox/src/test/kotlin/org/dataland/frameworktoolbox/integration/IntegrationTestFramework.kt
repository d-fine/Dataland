package org.dataland.frameworktoolbox.integration

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.utils.DatalandRepository
import java.io.File

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
                    "frameworktoolbox/integration/integrationTestFrameworkDefinition.csv",
            ),
        enabledFeatures =
            setOf(
                FrameworkGenerationFeatures.BackendDataModel,
                FrameworkGenerationFeatures.BackendApiController,
                FrameworkGenerationFeatures.ViewPage,
                FrameworkGenerationFeatures.FakeFixtures,
            ),
        order = 0,
    ) {
    override fun beforeFrontendTypecheck(datalandProject: DatalandRepository) {
        // Adding 'integrationTesting' to the backend's DataTypeEnum changes the inlined enums of
        // every downstream service that exposes a DataTypeEnum-typed parameter (e.g.
        // dataland-community-manager's GetDataRequestsDataTypeEnum). Regenerate every service's
        // OpenAPI spec and all clients so the frontend's generated clients agree on enum members
        // before the typecheck runs.
        datalandProject.gradleInterface.executeGradleTasks(
            listOf("generateOpenApiDocs", "generateClients"),
            force = true,
        )
    }
}
