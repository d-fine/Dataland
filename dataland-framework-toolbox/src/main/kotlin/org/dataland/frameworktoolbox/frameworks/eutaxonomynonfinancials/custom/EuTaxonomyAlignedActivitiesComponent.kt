package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.JsonExamples.EXAMPLE_PLAIN_EU_TAXONOMY_ALIGNED_ACTIVITIES_COMPONENT
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-Specific "EuTaxonomyAlignedActivities" component
 */
class EuTaxonomyAlignedActivitiesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(
        identifier, parent,
    ) {
    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable(" +
                    "${getTypescriptFieldAccessor()}, \"${
                        StringEscapeUtils.escapeEcmaScript(
                            label,
                        )
                    }\", \"" + getTypescriptFieldAccessor().split(".")[1].dropLast(1) + "\")",
                setOf(
                    TypeScriptImport(
                        "formatEuTaxonomyNonFinancialsAlignedActivitiesDataForTable",
                        "@/components/resources/dataTable/conversion/" +
                            "EuTaxonomyNonFinancialsAlignedActivitiesDataGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                "kotlin.collections.MutableList",
                true,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks" +
                            ".eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity",
                        false,
                    ),
                ),
            ),
            getSchemaAnnotationWithSuppressMaxLineLength(
                uploadPageExplanation,
                EXAMPLE_PLAIN_EU_TAXONOMY_ALIGNED_ACTIVITIES_COMPONENT,
            ),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "org.dataland.datalandqaservice.model.reports.QaReportDataPoint",
                isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.openApiClient.model" +
                            ".ExtendedDataPointListEuTaxonomyAlignedActivity",
                        isNullable,
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        val fixtureExpression =
            if (isNullable) {
                "dataGenerator.randomExtendedDataPoint(dataGenerator.randomArray(() => " +
                    "dataGenerator.generateAlignedActivity(), 0, 10))"
            } else {
                "dataGenerator.randomExtendedDataPoint(dataGenerator.guaranteedArray(() => " +
                    "dataGenerator.generateAlignedActivity(), 0, 10))"
            }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExpression,
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        return // is built by hand
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "EuTaxonomyAlignedActivitiesComponent",
        )
    }
}
