package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials202673.custom

import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesBaseComponent
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesFormatterConfig
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesGenerationConfig
import org.dataland.frameworktoolbox.frameworks.EuTaxonomyActivitiesTypeConfig
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent

/**
 * Represents the EuTaxonomy-Specific "EuTaxonomyEligibleOrAlignedActivities" component
 */
class EuTaxonomyEligibleOrAlignedActivitiesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : EuTaxonomyActivitiesBaseComponent(
        identifier,
        parent,
        EuTaxonomyActivitiesFormatterConfig(
            functionName = "formatEuTaxonomyNonFinancialsEligibleOrAlignedActivitiesDataForTable",
            factoryFileName = "EuTaxonomyNonFinancialsEligibleOrAlignedActivitiesDataGetterFactory",
        ),
        EuTaxonomyActivitiesTypeConfig(
            backendActivityType =
                "org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials202673.custom.EuTaxonomyEligibleOrAlignedActivity",
            extendedDataPointType =
                "org.dataland.datalandbackend.openApiClient.model.ExtendedDataPointListEuTaxonomyEligibleOrAlignedActivity",
        ),
        EuTaxonomyActivitiesGenerationConfig(
            fixtureGeneratorMethodName = "generateEligibleOrAlignedActivity",
            specificationType = "EuTaxonomyEligibleOrAlignedActivitiesComponent",
            uploadComponentNameOverride = "EligibleOrAlignedActivitiesFormField",
        ),
    )
