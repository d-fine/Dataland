package org.dataland.frameworktoolbox.frameworks.gdv.custom

import org.apache.commons.text.StringEscapeUtils
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.addStandardCellWithValueGetterFactory
import org.dataland.frameworktoolbox.intermediate.datapoints.SimpleDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getTypescriptFieldAccessor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * A GdvYearlyDecimalTimeseriesDataComponent is an in-memory representation of a generic field
 * that encodes several values across a span of multi years. It is displayed / upload in a matrix
 */
class GdvListOfBaseDataPointComponent(
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
        sectionConfigBuilder.addStandardCellWithValueGetterFactory( // TODO for now trying to display just the names
            this,
            FrameworkDisplayValueLambda(
                "{\n" +
                    "return formatListOfStringsForDatatable(" +
                    "${getTypescriptFieldAccessor()}?.map(it => it.value), " +
                    "'${StringEscapeUtils.escapeEcmaScript(label)}'" +
                    ")" +
                    "}",
                setOf(
                    "import { formatListOfStringsForDatatable } from " +
                        "\"@/components/resources/dataTable/conversion/MultiSelectValueGetterFactory\";",
                ),
            ),
        )
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
