package org.dataland.frameworktoolbox.frameworks

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
 * Configuration for EU Taxonomy activities component formatters.
 */
data class EuTaxonomyActivitiesFormatterConfig(
    val functionName: String,
    val factoryFileName: String,
)

/**
 * Type references for EU Taxonomy activities components.
 */
data class EuTaxonomyActivitiesTypeConfig(
    val backendActivityType: String,
    val extendedDataPointType: String,
)

/**
 * Generation configuration for EU Taxonomy activities components.
 */
data class EuTaxonomyActivitiesGenerationConfig(
    val fixtureGeneratorMethodName: String,
    val specificationType: String,
    val uploadComponentNameOverride: String? = null,
)

/**
 * Shared base component for EU Taxonomy activities list components.
 */
abstract class EuTaxonomyActivitiesBaseComponent(
    identifier: String,
    parent: FieldNodeParent,
    private val formatterConfig: EuTaxonomyActivitiesFormatterConfig,
    private val typeConfig: EuTaxonomyActivitiesTypeConfig,
    private val generationConfig: EuTaxonomyActivitiesGenerationConfig,
) : ComponentBase(
        identifier,
        parent,
    ) {
    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "${formatterConfig.functionName}(" +
                    "${getTypescriptFieldAccessor()}, \"${
                        StringEscapeUtils.escapeEcmaScript(
                            label,
                        )
                    }\", \"" + getTypescriptFieldAccessor().split(".")[1].dropLast(1) + "\")",
                setOf(
                    TypeScriptImport(
                        formatterConfig.functionName,
                        "@/components/resources/dataTable/conversion/${formatterConfig.factoryFileName}",
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
                        typeConfig.backendActivityType,
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
                        typeConfig.extendedDataPointType,
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
                    "dataGenerator.${generationConfig.fixtureGeneratorMethodName}(), 0, 10))"
            } else {
                "dataGenerator.randomExtendedDataPoint(dataGenerator.guaranteedArray(() => " +
                    "dataGenerator.${generationConfig.fixtureGeneratorMethodName}(), 0, 10))"
            }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExpression,
        )
    }

    override fun getUploadComponentName(): String = generationConfig.uploadComponentNameOverride ?: super.getUploadComponentName()

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        return // is built by hand
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            generationConfig.specificationType,
        )
    }
}
