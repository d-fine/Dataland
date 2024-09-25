package org.dataland.frameworktoolbox.frameworks.eutaxonomyfinancials

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * The EU Taxonomy Financials framework
 */
@Component
class EuTaxonomyFinancialsFramework : InDevelopmentPavedRoadFramework(
    identifier = "eu-taxonomy-financials",
    label = "EU Taxonomy Financials",
    explanation = "Additional Taxonomy for Financials",
    File("./dataland-framework-toolbox/inputs/eu-taxonomy-financials/eu-taxonomy-financials.xlsx"),
    order = 1,
    enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
)
