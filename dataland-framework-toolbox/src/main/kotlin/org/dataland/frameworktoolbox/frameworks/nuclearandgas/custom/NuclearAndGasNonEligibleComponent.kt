package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the EuTaxonomy-specific "NuclearAndGasNonEligible" component
 */
class NuclearAndGasNonEligibleComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    private val fullyQualifiedNameOfKotlinType =
        "org.dataland.datalandbackend.frameworks.nuclearandgas.custom.NuclearAndGasNonEligible"

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint",
                isNullable,
                listOf(
                    TypeReference(
                        fullyQualifiedNameOfKotlinType,
                        false,
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "org.dataland.datalandqaservice.model.reports.QaReportDataPoint",
                isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.openApiClient.model." +
                            "ExtendedDataPointNuclearAndGasNonEligible",
                        isNullable,
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        return // TODO: fill with code
    }



    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatNuclearAndGasTaxonomyShareDataForTable(" +
                        "${getTypescriptFieldAccessor(true)}, \"${
                            StringEscapeUtils.escapeEcmaScript(
                                label,
                            )
                        }\")",
                setOf(
                    TypeScriptImport(
                        "formatNuclearAndGasTaxonomyShareDataForTable",
                        "@/components/resources/dataTable/conversion/" +
                                "NuclearAndGasValueGetterFactory",
                    ),
                ),
            ),
        )
    }




    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.randomExtendedDataPoint(" +
                "dataGenerator.generateNuclearAndGasNonEligible())",
        )
    }
}
