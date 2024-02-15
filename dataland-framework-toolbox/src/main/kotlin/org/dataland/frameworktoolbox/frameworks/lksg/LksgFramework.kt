package org.dataland.frameworktoolbox.frameworks.lksg

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

@Component
class LksgFramework: InDevelopmentPavedRoadFramework (
    identifier = "lksg",
    label = "LkSG",
    explanation = "Lieferkettensorgfaltspflichtengesetz",
    File("./dataland-framework-toolbox/inputs/lksg/lksg.xlsx"),
    order = 6,
) {

}