package org.dataland.frameworktoolbox.frameworks.gdv.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * A GdvYearlyDecimalTimeseriesDataComponent is an in-memory representation of a generic field
 * that encodes several values across a span of multi years. It is displayed / upload in a matrix
 */
class GdvListOfBaseDataPoint(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            this.identifier,
            TypeReference(
                "List",
                isNullable,
                listOf(
                    SimpleDocumentSupport.getJvmTypeReference(
                        TypeReference("String", false),
                        false,
                    ),
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        // TODO Emanuel: Do as soon as display component available in framework
        /*val configurationObject = mutableMapOf<String, Map<String, String>>()
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
        )*/
        println("nothing") // TODO
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.valueOrNull(" +
                "generateArray(" +
                "() => dataGenerator.guaranteedBaseDataPoint(dataGenerator.guaranteedShortString()), 1, 5, 0" +
                "))",
            setOf("import { generateArray } from \"@e2e/fixtures/FixtureUtils\";"),

        )
    }
}
