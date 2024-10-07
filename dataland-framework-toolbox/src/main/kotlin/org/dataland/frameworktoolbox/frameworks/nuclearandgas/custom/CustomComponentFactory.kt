package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.ParsedExcel
import org.dataland.frameworktoolbox.template.TemplateComponentBuilder
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateDocumentSupport
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.dataland.frameworktoolbox.utils.Naming
import org.dataland.frameworktoolbox.utils.capitalizeEn
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
data class CustomComponentRow(
    @JsonProperty("Field Identifier")
    var fieldIdentifier: String,

    @JsonProperty("Template")
    var template: String,

    @JsonProperty("Field Name")
    var fieldName: String,

    @JsonProperty("Tooltip")
    var tooltip: String,

    @JsonProperty("Component")
    var component: String,
) {
    fun toTemplateRow(): TemplateRow {
        return TemplateRow(
            fieldIdentifier,
            "",
            "",
            fieldName,
            tooltip,
            null,
            null,
            component,
            "",
            "",
            TemplateDocumentSupport.None,
            "",
            "",
            TemplateYesNo.No,
        )
    }
}

class CustomComponentFactory(
    val templateDiagnostic: TemplateDiagnostic,
    baseFrameworkName: String,
    private val componentName: String,
    private val customComponentRows: List<CustomComponentRow>,
) : TemplateComponentFactory {

    companion object {

        private const val SHEET_NAME = "Custom Component Data"
        const val PACKAGE_NAME = "custom"

        fun fromExcel(file: File, templateDiagnostic: TemplateDiagnostic, baseFrameworkName: String): List<CustomComponentFactory> {
            val parsedExcel = ParsedExcel.fromXlsx<CustomComponentRow>(file, SHEET_NAME)
            val rowsByTemplate = parsedExcel.rows
                .groupBy { it.template.trim() }
            return rowsByTemplate.entries.map {
                CustomComponentFactory(templateDiagnostic, baseFrameworkName, it.key, it.value)
            }
        }
    }

    val framework = Framework("$baseFrameworkName Custom Component $componentName", "", "", -1)
    private val className = Naming.getNameFromLabel("$baseFrameworkName $componentName").capitalizeEn()

    fun buildInternalFramework(componentFactories: List<TemplateComponentFactory>) {
        val templateRows = customComponentRows.map(CustomComponentRow::toTemplateRow).toMutableList()
        val excelTemplate = ExcelTemplate(templateRows)

        val generationUtils = ComponentGenerationUtils()

        val intermediateBuilder = TemplateComponentBuilder(
            template = excelTemplate,
            componentFactories = componentFactories,
            generationUtils = generationUtils,
        )
        intermediateBuilder.build(into = framework.root)
    }

    fun addClassToPackageBuilder(packageBuilder: PackageBuilder) {
        val dataClassBuilder =
            packageBuilder.addClass(className, "TODO")
        framework.root.children.forEach {
            it.generateDataModel(dataClassBuilder)
        }
    }

    override fun canGenerateComponent(row: TemplateRow): Boolean =
        row.component.trim() == "CustomComponent $componentName"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        templateDiagnostic.optionsNotUsed(row)
        templateDiagnostic.unitNotUsed(row)

        return componentGroup.create<CustomComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            utils.setCommonProperties(row, this)
            this.qualifiedNameRelativeToFrameworkRoot = "$PACKAGE_NAME.$className"
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
