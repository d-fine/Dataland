package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.DecimalComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Generates DecimalComponents from rows with the component "Number"
 */
@Component
class DecimalComponentFactory(
    @Autowired val templateDiagnostic: TemplateDiagnostic,
) : TemplateComponentFactory {
    companion object {
        /**
         * Pareses bounds for a numeric variable given in the format [LOWER BOUND, UPPER BOUND].
         * An "INF" may be used to specify that there is no bound
         */
        fun parseBounds(input: String): Pair<Long?, Long?> {
            if (input.isBlank()) return Pair(null, null)

            val pattern =
                """Allowed Range:\s*\[\s*(?<lower>(?:\-?\d+|-INF))\s*,\s*(?<upper>(?:\-?\d+|INF))\s*\]""".toRegex()
            val matchResult =
                pattern.find(input)
                    ?: throw IllegalArgumentException(
                        "Decimal options $input does not" +
                            " match the expected format 'Allowed Range: [LOWER_BOUND, UPPER_BOUND]'",
                    )

            val lowerBoundGroupMatch = matchResult.groups["lower"]!!.value
            val upperBoundGroupMatch = matchResult.groups["upper"]!!.value

            fun getBound(value: String): Long? =
                if (value == "INF" || value == "-INF") {
                    null
                } else {
                    value.toLong()
                }

            return Pair(getBound(lowerBoundGroupMatch), getBound(upperBoundGroupMatch))
        }
    }

    override fun canGenerateComponent(row: TemplateRow): Boolean = row.component == "Number"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        val bounds = parseBounds(row.options)

        return componentGroup.create<DecimalComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            utils.setCommonProperties(row, this)
            if (row.unit.isNotBlank()) {
                constantUnitSuffix = row.unit.trim()
            }
            this.minimumValue = bounds.first
            this.maximumValue = bounds.second
        }
    }

    override fun updateDependency(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentIdentifierMap: Map<String, ComponentBase>,
    ) {
        utils.defaultDependencyConfiguration(row, componentIdentifierMap, templateDiagnostic)
    }
}
