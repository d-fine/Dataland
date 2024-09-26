package org.dataland.frameworktoolbox.frameworks.nuclearandgas
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * The EU Taxonomy Nuclear And Gas Framework
 */
@Component
class NuclearAndGasFramework : InDevelopmentPavedRoadFramework(
    identifier = "nuclear-and-gas",
    label = "EU Taxonomy Nuclear and Gas Framework",
    explanation = "EU Taxonomy Nuclear and Gas Framework according to the Commission Delegated Regulation (EU)" +
        " 2021/2178, Annex XII ",
    File("./dataland-framework-toolbox/inputs/nuclear-and-gas/nuclear-and-gas.xlsx"),
    order = 3,
    enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
)
