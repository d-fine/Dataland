package org.dataland.frameworktoolbox.frameworks.gdv.custom

import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.SectionUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * A GdvYearlyDecimalTimeseriesDataComponent is an in-memory representation of a generic field
 * that encodes several values across a span of multi years. It is displayed / upload in a matrix
 */
class GdvYearlyDecimalTimeseriesDataComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    /**
     * TODO
     */
    data class TimeseriesRow(val identifier: String, val label: String, val unitSuffix: String)

    var decimalRows: MutableList<TimeseriesRow> = mutableListOf()

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        require(decimalRows.isNotEmpty()) {
            "Please add at least one decimal row to this GdvYearlyDecimalTimeseriesDataComponent " +
                "to receive useful output."
        }

        val fieldDataClass = dataClassBuilder.parentPackage.addClass(
            name = "${this.identifier.capitalizeEn()}Values",
            comment = "Data class for the timeseries data contained in the field ${this.identifier}",
        )

        for (decimalRow in decimalRows) {
            fieldDataClass.addProperty(
                decimalRow.identifier,
                TypeReference("java.math.BigDecimal", true),
            )
        }

        dataClassBuilder.addProperty(
            this.identifier,
            documentSupport.getJvmTypeReference(
                TypeReference(
                    "org.dataland.datalandbackend.model.gdv.YearlyTimeseriesData",
                    isNullable,
                    listOf(fieldDataClass.getTypeReference(isNullable)),
                ),
                isNullable,
            ),
        )
    }

    override fun generateDefaultUploadConfig(sectionUploadConfigBuilder: SectionUploadConfigBuilder) {
        sectionUploadConfigBuilder.addStandardCellWithValueGetterFactory(
            uploadComponentName = "GdvYearlyDecimalTimeseriesDataFormField",
            options = decimalRows.map {
                SelectionOption(it.identifier, it.label)
            }.toMutableSet(),
            component = this,
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        val configurationObject = mutableMapOf<String, Map<String, String>>()
        for (row in decimalRows) {
            configurationObject[row.identifier] = mapOf(
                "label" to row.label,
                "unitSuffix" to row.unitSuffix,
            )
        }

        val objectMapper = jacksonObjectMapper()
        objectMapper.disable(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature())
        val configurationObjectString = objectMapper.writeValueAsString(configurationObject)

        sectionConfigBuilder.addStandardCellWithValueGetterFactory(
            this,
            FrameworkDisplayValueLambda(
                "formatGdvYearlyDecimalTimeseriesDataForTable(" +
                    "${getTypescriptFieldAccessor(true)}, " +
                    "$configurationObjectString, " +
                    "'${StringEscapeUtils.escapeEcmaScript(label)}')",
                setOf(
                    "import { formatGdvYearlyDecimalTimeseriesDataForTable } from " +
                        "\"@/components/resources/dataTable/conversion/gdv" +
                        "/GdvYearlyDecimalTimeseriesDataGetterFactory\";",
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        require(decimalRows.isNotEmpty()) {
            "Please add at least one decimal row to this GdvYearlyDecimalTimeseriesDataComponent " +
                "component to receive useful output."
        }
        val jsIdentifierArray = "[${decimalRows.joinToString(", ")
            { '"' + StringEscapeUtils.escapeEcmaScript(it.identifier) + '"' }}]"

        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression = "dataGenerator.guaranteedDecimalYearlyTimeseriesData($jsIdentifierArray)",
                nullableFixtureExpression = "dataGenerator.randomDecimalYearlyTimeseriesData($jsIdentifierArray)",
                nullable = isNullable,
            ),
        )
    }
}
