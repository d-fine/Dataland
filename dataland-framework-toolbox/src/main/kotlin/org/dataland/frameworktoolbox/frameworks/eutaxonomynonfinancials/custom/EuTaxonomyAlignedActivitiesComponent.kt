package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesBaseComponent
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesFormatterConfig
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesGenerationConfig
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesTypeConfig
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent

/**
 * Represents the EuTaxonomy-Specific "EuTaxonomyAlignedActivities" component
 */
class EuTaxonomyAlignedActivitiesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : EuTaxonomyActivitiesBaseComponent(
        identifier,
        parent,
        EuTaxonomyActivitiesFormatterConfig(
            functionName = "formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable",
            factoryFileName = "EuTaxonomyNonFinancialsAlignedActivitiesDataGetterFactory",
        ),
        EuTaxonomyActivitiesTypeConfig(
            backendActivityType =
                "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity",
            extendedDataPointType =
                "org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointListEuTaxonomyAlignedActivity",
        ),
        EuTaxonomyActivitiesGenerationConfig(
            fixtureGeneratorMethodName = "generateAlignedActivity",
            specificationType = "EuTaxonomyAlignedActivitiesComponent",
        ),
    )
