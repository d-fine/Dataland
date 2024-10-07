package org.dataland.frameworktoolbox.template

import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.dataland.frameworktoolbox.utils.shortSha
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A diagnostic utility to log common warning encountered during Excel-Conversion
 */
@Component
class TemplateDiagnostic(
    @Autowired val diagnostic: DiagnosticManager,
) {
    /**
     * Attests that this generator does not use the "options" column of the CSV
     */
    fun optionsNotUsed(row: TemplateRow) = unusedColumn("options", "", row, row.options)

    /**
     * Attests that this generator does not use the "unit" column of the CSV
     */
    fun unitNotUsed(row: TemplateRow) = unusedColumn("unit", "", row, row.unit)

    /**
     * Attests that this generator does not use the "documentSupport" column of the CSV
     */
    fun documentSupportNotUsed(row: TemplateRow) = unusedColumn("documentSupport", "None", row, row.documentSupport.toString())

    /**
     * Attests that this generator does not use the "Show when value is" column of the CSV
     */
    fun showWhenValueIsNotUsed(row: TemplateRow) = unusedColumn("showWhenValueIs", "", row, row.showWhenValueIs)

    /**
     * Attests that this generator does not use "No" within the mandatory column of the CSV
     */
    fun mandatoryIsNotUsed(row: TemplateRow) =
        unusedColumn("mandatoryField", "No", row, row.mandatoryField.toString())

    private fun unusedColumn(
        columnName: String,
        expectedColumnValue: String,
        row: TemplateRow,
        columnValue: String,
    ) {
        diagnostic.warnIf(
            columnValue.trim() != expectedColumnValue,
            "TemplateConversion-UnusedColumn-$columnName-Row-${row.fieldIdentifier}-${columnValue.shortSha()}",
            "The row ${row.fieldIdentifier} defined a non-standard value for the column " +
                "'$columnName' ($columnValue), which was not considered during conversion",
        )
    }
}
