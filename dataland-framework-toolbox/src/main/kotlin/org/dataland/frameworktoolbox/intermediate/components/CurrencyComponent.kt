package org.dataland.frameworktoolbox.intermediate.components

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.basecomponents.NumberBaseComponent
import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

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
        require(documentSupport is ExtendedDocumentSupport) {
            "CurrencyComponent only supports Extended document support"
        }
        val annotations = getMinMaxDatamodelAnnotations(minimumValue, maximumValue)

        dataClassBuilder.addProperty(
            identifier,
            TypeReference("org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint", isNullable),
            annotations,
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        require(documentSupport is ExtendedDocumentSupport) {
            "CurrencyComponent only supports Extended document support"
        }
        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatCurrencyForDisplay(${getTypescriptFieldAccessor()}, \"${
                    StringEscapeUtils.escapeEcmaScript(
                        label,
                    )
                }\")",
                setOf(
                    "import { formatCurrencyForDisplay } from " +
                        "\"@/components/resources/dataTable/conversion/CurrencyDataPointValueGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        require(documentSupport is ExtendedDocumentSupport) {
            "CurrencyComponent only supports Extended document support"
        }
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "CurrencyDataPointFormField",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        require(documentSupport is ExtendedDocumentSupport) {
            "CurrencyComponent only supports Extended document support"
        }
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
