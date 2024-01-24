package org.dataland.frameworktoolbox.frameworks.euTaxonomyNonFinancials

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.Naming
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * Custom utility class used during Template conversion for the Eu Taxonomy Non-Financials framework.
 */
class EuTaxonomyNonFinancialsComponentGenerationUtils : ComponentGenerationUtils() {
    private fun getFieldNameFromGermanString(technicalFieldName: String): String {
        return Naming.getNameFromLabel(
            technicalFieldName
                .replace("ö", "oe")
                .replace("Ö", "Oe")
                .replace("Ä", "Ae")
                .replace("ä", "ae")
                .replace("Ü", "Ue")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replace("\"", "")
                .replace(".", "")
                .replace(";", ""),
        )
    }

    override fun generateSectionIdentifierFromRow(row: TemplateRow): String {
        return getFieldNameFromGermanString(row.category)
    }

    override fun generateSubSectionIdentifierFromRow(row: TemplateRow): String {
        return getFieldNameFromGermanString(row.subCategory)
    }

    override fun generateFieldIdentifierFromRow(row: TemplateRow): String {
        return getFieldNameFromGermanString(row.fieldName)
    }

    override fun getSelectionOptionsFromOptionColumn(row: TemplateRow): Set<SelectionOption> {
        val stringOptions = row.options
            .split("|")
            .map { it.trim() }

        val mappedOptions = stringOptions.map {
            SelectionOption(
                identifier = getFieldNameFromGermanString(it).capitalizeEn(),
                label = it,
            )
        }.toSet()

        require(mappedOptions.isNotEmpty()) {
            "Field ${row.fieldIdentifier} does not specify required options for component ${row.component}."
        }
        return mappedOptions
    }
}
