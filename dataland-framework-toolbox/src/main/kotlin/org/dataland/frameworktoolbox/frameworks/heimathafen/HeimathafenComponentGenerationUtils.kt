package org.dataland.frameworktoolbox.frameworks.heimathafen

import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.Naming

/**
 * The HeimathafenComponentGenerationUtils implement framework-specific field-name generation
 * for the heimathafen framework
 */
class HeimathafenComponentGenerationUtils(private val excelTemplate: ExcelTemplate) : ComponentGenerationUtils() {

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
                .replace("?", ""),
        )
    }

    override fun generateSectionIdentifierFromRow(row: TemplateRow): String {
        return getFieldNameFromGermanString(row.category)
    }

    override fun generateSubSectionIdentifierFromRow(row: TemplateRow): String {
        return getFieldNameFromGermanString(row.subCategory)
    }

    private fun injectDependencyIntoFieldName(row: TemplateRow, pattern: String): String {
        val dependsOnRow = excelTemplate.rows.find { it.fieldIdentifier == row.dependency }
        requireNotNull(dependsOnRow)
        return pattern.replace("{{dependencyFieldName}}", dependsOnRow.fieldName)
    }

    @Suppress("CyclomaticComplexMethod")
    private fun getTechnicalFieldNameFromRow(row: TemplateRow): String {
        return when (true) {
            (row.fieldName == "Wenn Nein, bitte begründen") ->
                injectDependencyIntoFieldName(row, "Wenn {{dependencyFieldName}} Nein, bitte begründen")
            (row.fieldName == "Verwendete Schlüsselzahlen") ->
                injectDependencyIntoFieldName(row, "Verwendete Schlüsselzahlen für {{dependencyFieldName}}")
            (row.fieldName == "Datenerfassung" && row.dependency.isNotBlank()) ->
                injectDependencyIntoFieldName(row, "Datenerfassung für {{dependencyFieldName}}")
            (row.fieldName == "Daten Plausibilitätsprüfung" && row.dependency.isNotBlank()) ->
                injectDependencyIntoFieldName(row, "Daten Plausibilitätsprüfung für {{dependencyFieldName}}")
            (row.fieldName == "Datenquelle" && row.dependency.isNotBlank()) ->
                injectDependencyIntoFieldName(row, "Datenquelle für {{dependencyFieldName}}")
            (row.fieldName == "Metrisch verwendet" && row.dependency.isNotBlank()) ->
                injectDependencyIntoFieldName(row, "Metrisch verwendet für {{dependencyFieldName}}")
            (row.fieldName == "Methodik der Berechnung" && row.dependency.isNotBlank()) ->
                injectDependencyIntoFieldName(row, "Methodik der Berechnung für {{dependencyFieldName}}")
            (row.fieldName == "Verwendete Quellen" && row.dependency.isNotBlank()) ->
                injectDependencyIntoFieldName(row, "Verwendete Quellen für {{dependencyFieldName}}")
            (row.fieldName == "24/7 Verfügbarkeit") ->
                "Jederzeit Verfügbar"
            else -> row.fieldName
        }
    }

    override fun generateFieldIdentifierFromRow(row: TemplateRow): String {
        val technicalFieldName = getTechnicalFieldNameFromRow(row)
        return getFieldNameFromGermanString(technicalFieldName)
    }
}
