package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
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
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference(
                "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint",
                isNullable,
                listOf(
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
                ),
            ),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            this.identifier,
            TypeReference(
                "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint",
                isNullable,
                listOf(
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
}
