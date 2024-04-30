package org.dataland.frameworktoolbox.frameworks.sme

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the SME framework
 */
@Component
class SmeFramework : PavedRoadFramework(
    identifier = "sme",
    label = "SME",
    explanation = "Small and medium-sized enterprises questionnaire",
    File("./dataland-framework-toolbox/inputs/sme/sme.xlsx"),
    order = 6,
    enabledFeatures = FrameworkGenerationFeatures.allExcept(FrameworkGenerationFeatures.BackendApiController),
)
