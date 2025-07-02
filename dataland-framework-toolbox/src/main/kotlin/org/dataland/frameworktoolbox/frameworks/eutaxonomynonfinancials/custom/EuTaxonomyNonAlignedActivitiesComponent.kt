package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.JsonExamples.EXAMPLE_PLAIN_EU_TAXONOMY_NON_ALIGNED_ACTIVITIES_COMPONENT
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.SuppressKtlintMaxLineLengthAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-Specific "EuTaxonomyNonAlignedActivities" component
 */
class EuTaxonomyNonAlignedActivitiesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val schemaAnnotation =
            Annotation(
                fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                rawParameterSpec =
                    "description = \"\"\"${uploadPageExplanation}\"\"\", \n" +
                        "example = \"\"\"${getExample(EXAMPLE_PLAIN_EU_TAXONOMY_NON_ALIGNED_ACTIVITIES_COMPONENT)} \"\"\"",
                applicationTargetPrefix = "field",
            )
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                "kotlin.collections.MutableList",
                true,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks" +
                            ".eutaxonomynonfinancials.custom.EuTaxonomyActivity",
                        false,
                    ),
                ),
            ),
            listOf(SuppressKtlintMaxLineLengthAnnotation, schemaAnnotation),
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
                            ".ExtendedDataPointListEuTaxonomyActivity",
                        isNullable,
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "NonAlignedActivitiesFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.randomExtendedDataPoint(dataGenerator.randomArray(() => " +
                "dataGenerator.generateActivity(), 0, 2))",
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatNonAlignedActivitiesForDataTable(" +
                    "${this.getTypescriptFieldAccessor()}," +
                    "\"${StringEscapeUtils.escapeEcmaScript(label)}\"," +
                    ")",
                setOf(
                    TypeScriptImport(
                        "formatNonAlignedActivitiesForDataTable",
                        "@/components/resources/dataTable/conversion/EutaxonomyNonAlignedActivitiesValueGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        specificationCategoryBuilder.addDefaultDatapointAndSpecification(
            this,
            "EuTaxonomyNonAlignedActivitiesComponent",
        )
    }
}
