package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.getBackendClientTypeReference
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A CurrencyComponent represents a numeric value with currency
 */
class CurrencyComponent(
    identifier: String,
    parent: FieldNodeParent,
) : NumberBaseComponent(identifier, parent) {

    var minimumValue: Long? = null
    var maximumValue: Long? = null

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        val annotations = getMinMaxDatamodelAnnotations(minimumValue, maximumValue) + ValidAnnotation

        dataClassBuilder.addProperty(
            identifier,
            TypeReference("org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint", isNullable),
            annotations,
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        val backendCurrencyDatapoint = TypeReference(
            "org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint",
            isNullable,
        )
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "org.dataland.datalandqaservice.model.reports.QaReportDataPoint",
                true,
                listOf(
                    backendCurrencyDatapoint.getBackendClientTypeReference(),
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatCurrencyForDisplay(${getTypescriptFieldAccessor()}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    TypeScriptImport(
                        "formatCurrencyForDisplay",
                        "@/components/resources/dataTable/conversion/CurrencyDataPointValueGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "CurrencyDataPointFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(ExtendedDocumentSupport))
        val rangeParameterSpecification = getFakeFixtureMinMaxRangeParameterSpec(minimumValue, maximumValue)
        val expression = if (isRequired) {
            "dataGenerator.guaranteedCurrencyDataPoint($rangeParameterSpecification)"
        } else {
            "dataGenerator.randomCurrencyDataPoint($rangeParameterSpecification)"
        }

        sectionBuilder.addAtomicExpression(
            identifier,
            expression,
        )
    }
}
