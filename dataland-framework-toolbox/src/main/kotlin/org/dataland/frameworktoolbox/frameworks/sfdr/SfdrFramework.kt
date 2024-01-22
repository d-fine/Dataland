package org.dataland.frameworktoolbox.frameworks.sfdr

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the SFDR framework
 */
@Component
class SfdrFramework : InDevelopmentPavedRoadFramework(
    identifier = "sfdr",
    label = "SFDR",
    explanation = "Sustainability Finance Disclosure Regulation",
    File("./dataland-framework-toolbox/data-dictionary/csv/dataDictionary-SFDR (developer version).csv"),
) {

}
