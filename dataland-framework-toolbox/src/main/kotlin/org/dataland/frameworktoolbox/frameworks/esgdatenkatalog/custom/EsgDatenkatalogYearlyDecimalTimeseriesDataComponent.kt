package org.dataland.frameworktoolbox.frameworks.esgdatenkatalog.custom

import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.components.addStandardUploadConfigCell
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import org.dataland.frameworktoolbox.utils.typescript.generateTsCodeForOptionsOfSelectionFormFields

/**
 * A EsgDatenkatalogYearlyDecimalTimeseriesDataComponent is an in-memory representation of a generic field
 * that encodes several values across a span of multi years. It is displayed / upload in a matrix
 */
class EsgDatenkatalogYearlyDecimalTimeseriesDataComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    companion object {
        const val THREE_YEARS = 3
    }

    /**
     * A TimeseriesRow specifies a single property that is to be tracked across time
     */
    data class TimeseriesRow(
        val identifier: String,
        val label: String,
        val unitSuffix: String,
    )

    /**
     * The UploadBehaviour specifies how many years this component is expected to be filled out with
     * during upload
     */
    enum class UploadBehaviour(
        val yearsIntoPast: Int,
        val yearsIntoFuture: Int,
    ) {
        ThreeYearDelta(THREE_YEARS, THREE_YEARS),
        ThreeYearPast(THREE_YEARS, 0),
    }

    var decimalRows: MutableList<TimeseriesRow> = mutableListOf()
    var uploadBehaviour: UploadBehaviour = UploadBehaviour.ThreeYearDelta

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        require(decimalRows.isNotEmpty()) {
            "Please add at least one decimal row to this EsgDatenkatalogYearlyDecimalTimeseriesDataComponent " +
                "to receive useful output."
        }

        val fieldDataClass =
            dataClassBuilder.parentPackage.addClass(
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
                    "org.dataland.datalandbackend.frameworks.esgdatenkatalog.custom.YearlyTimeseriesData",
                    isNullable,
                    listOf(fieldDataClass.getTypeReference(isNullable)),
                ),
                isNullable,
            ),
            documentSupport.getJvmAnnotations(),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val componentName =
            when (uploadBehaviour) {
                UploadBehaviour.ThreeYearDelta -> "EsgDatenkatalogYearlyDecimalTimeseriesThreeYearDeltaDataFormField"
                UploadBehaviour.ThreeYearPast -> "EsgDatenkatalogYearlyDecimalTimeseriesThreeYearPastDataFormField"
            }

        uploadCategoryBuilder.addStandardUploadConfigCell(
            frameworkUploadOptions =
                FrameworkUploadOptions(
                    body =
                        generateTsCodeForOptionsOfSelectionFormFields(
                            decimalRows
                                .map {
                                    var rowLabel = it.label
                                    if (it.unitSuffix.isNotBlank()) {
                                        rowLabel += " (in ${it.unitSuffix})"
                                    }

                                    SelectionOption(it.identifier, rowLabel)
                                }.toSet(),
                        ),
                    imports = null,
                ),
            component = this,
            uploadComponentName = componentName,
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        val configurationObject = mutableMapOf<String, Map<String, String>>()
        for (row in decimalRows) {
            configurationObject[row.identifier] =
                mapOf(
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
                "formatEsgDatenkatalogYearlyDecimalTimeseriesDataForTable(" +
                    "${getTypescriptFieldAccessor(true)}, " +
                    "$configurationObjectString, " +
                    "'${StringEscapeUtils.escapeEcmaScript(label)}')",
                setOf(
                    TypeScriptImport(
                        "formatEsgDatenkatalogYearlyDecimalTimeseriesDataForTable",
                        "@/components/resources/dataTable/conversion/esg-datenkatalog" +
                            "/EsgDatenkatalogYearlyDecimalTimeseriesDataGetterFactory",
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        require(decimalRows.isNotEmpty()) {
            "Please add at least one decimal row to this EsgDatenkatalogYearlyDecimalTimeseriesDataComponent " +
                "component to receive useful output."
        }
        val jsIdentifierArray = "[${decimalRows.joinToString(", ")
            { '"' + StringEscapeUtils.escapeEcmaScript(it.identifier) + '"' }}]"

        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                fixtureExpression =
                    "dataGenerator.guaranteedDecimalYearlyTimeseriesData" +
                        "($jsIdentifierArray, ${uploadBehaviour.yearsIntoPast}, ${uploadBehaviour.yearsIntoFuture})",
                nullableFixtureExpression =
                    "dataGenerator.randomDecimalYearlyTimeseriesData" +
                        "($jsIdentifierArray, ${uploadBehaviour.yearsIntoPast}, ${uploadBehaviour.yearsIntoFuture})",
                nullable = isNullable,
            ),
        )
    }
}
