package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * Represents the ProcurementCategories component
 */
class LksgSubcontractingCompaniesComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "Map", isNullable,
                listOf(
                    TypeReference(
                        "String",
                        false,
                    ),
                    TypeReference(
                        "List",
                        false,
                        listOf(TypeReference("String", false)),
                    ),
                ),
            ),
            listOf(
                Annotation(
                    fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                    rawParameterSpec =
                    "example = JsonExampleFormattingConstants.SUBCONTRACTING_COMPANIES_DEFAULT_VALUE",
                    applicationTargetPrefix = "field",
                    additionalImports = setOf("org.dataland.datalandbackend.utils.JsonExampleFormattingConstants"),
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatLksgSubcontractingCompaniesForDisplay(${getTypescriptFieldAccessor(true)}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        "formatLksgSubcontractingCompaniesForDisplay",
                        "@/components/resources/dataTable/conversion/lksg/LksgDisplayValueGetters",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "SubcontractingCompaniesFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        val fixtureExperssion = if (isNullable) {
            "dataGenerator.valueOrNull(dataGenerator.generateSubcontractingCompanies())"
        } else {
            "dataGenerator.generateSubcontractingCompanies()"
        }
        sectionBuilder.addAtomicExpression(
            identifier,
            fixtureExperssion,
        )
    }
}
