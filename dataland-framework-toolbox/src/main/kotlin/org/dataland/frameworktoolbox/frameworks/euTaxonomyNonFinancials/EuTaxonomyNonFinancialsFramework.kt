package org.dataland.frameworktoolbox.frameworks.euTaxonomyNonFinancials

import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * Definition of the Eu Taxonomy Non-Financials framework
 */
@Component
class EuTaxonomyNonFinancialsFramework : InDevelopmentPavedRoadFramework(
    identifier = "eutaxonomy-non-financials",
    label = "EU Taxonomy Non-Financials",
    explanation = "Das Eu Taxonomy Non-Financials Framework",
    File(
        "./dataland-framework-toolbox/inputs/euTaxonomyNonFinancials/EutaxonomyNonFinancials.xlsx",
    ),
    order = 2,
)
