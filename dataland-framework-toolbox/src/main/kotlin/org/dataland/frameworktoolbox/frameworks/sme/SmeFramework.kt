package org.dataland.frameworktoolbox.frameworks.sfdr

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the SME framework
 */
@Component
class SmeFramework : InDevelopmentPavedRoadFramework(
    identifier = "sme",
    label = "SME",
    explanation = "Small and medium-sized enterprises questionnaire",
    File("./dataland-framework-toolbox/inputs/sme/sme.xlsx"),
    order = 4,
)
