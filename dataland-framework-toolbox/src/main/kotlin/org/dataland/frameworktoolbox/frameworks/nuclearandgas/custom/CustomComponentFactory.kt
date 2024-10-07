package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.FrameworkFixtureGeneratorBuilder
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

/**
 * The CustomComponentRow class is used to parse the Rows of the csv file (obtained by the respective sheet of the
 * Excel file) into a data structure.
 * This data structure represents the data stored in the respective Excel sheet.
 */
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
    /**
     * This function converts the CustomComponentRow object into a TemplateRow object, that is usually used to define
     * frameworks. The [isMandatory] parameter specifies whether all rows are mandatory or not.
     */
    fun toTemplateRow(isMandatory: TemplateYesNo): TemplateRow {
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
            isMandatory,
        )
    }
}

/**
 * The CustomComponentFactory class implements the logic for the creation of custom components defined in a
 * separate Excel sheet.
 * Objects of this class are typically create with the [fromExcel] function. This function parses the Excel
 * and creates as many factories as they are defined within the Excel sheet.
 */
class CustomComponentFactory(
    val templateDiagnostic: TemplateDiagnostic,
    baseFrameworkName: String,
    private val componentName: String,
    private val customComponentRows: List<CustomComponentRow>,
) : TemplateComponentFactory {

    companion object {

        const val NULLABLE_GENERATOR_PREFIX = "random"

        private const val SHEET_NAME = "Custom Component Data"
        const val PACKAGE_NAME = "custom"

        /**
         * This function is used to parse the content of the Excel file linked by [file] in the sheet named [SHEET_NAME]
         * and creates the respective CustomComponentFactory.
         * The [templateDiagnostic] object is used to within these factories to report errors while generating their
         * internal framework representation.
         * The [baseFrameworkName] refers to the name of the framework that uses these factories.
         */
        fun fromExcel(
            file: File,
            templateDiagnostic: TemplateDiagnostic,
            baseFrameworkName: String,
        ): List<CustomComponentFactory> {
            val parsedExcel = ParsedExcel.fromXlsx<CustomComponentRow>(file, SHEET_NAME)
            val rowsByTemplate = parsedExcel.rows
                .groupBy { it.template.trim() }
            return rowsByTemplate.entries.map {
                CustomComponentFactory(templateDiagnostic, baseFrameworkName, it.key, it.value)
            }
        }
    }

    private val framework = Framework("$baseFrameworkName Custom Component $componentName", "", "", -1)

    private val className = Naming.getNameFromLabel("$baseFrameworkName $componentName").capitalizeEn()
    private val classComment = "The data-model for the $className custom component."

    /**
     * After creating the CustomComponentFactory the content of the respective Excel sheet is parsed.
     * This function build the in-memory representation of the internal framework used by this factory.
     * To build the internal framework we need the [componentFactories] to create the component defined in the
     * internal framework.
     */
    fun buildInternalFramework(componentFactories: List<TemplateComponentFactory>) {
        val templateRows = customComponentRows
            .map { it.toTemplateRow(TemplateYesNo.No) }
            .toMutableList()
        val excelTemplate = ExcelTemplate(templateRows)

        val intermediateBuilder = TemplateComponentBuilder(
            template = excelTemplate,
            componentFactories = componentFactories,
            generationUtils = ComponentGenerationUtils(),
        )
        intermediateBuilder.build(into = framework.root)
    }

    /**
     * In order to use the custom component that is emitted by this factory we need to create additional files that
     * define the data-model of the custom component.
     * This function adds these files to the [packageBuilder].
     */
    fun addClassToPackageBuilder(packageBuilder: PackageBuilder) {
        val dataClassBuilder = packageBuilder.addClass(className, classComment)
        framework.root.children.forEach {
            it.generateDataModel(dataClassBuilder)
        }
    }

    /**
     * In order to generate fake fixtures for the custom component we need additional fake fixture generators.
     * This function adds these fake fixture generators into the [frameworkFixtureGeneratorBuilder].
     */
    fun addGeneratorToFixtureGeneratorBuilder(frameworkFixtureGeneratorBuilder: FrameworkFixtureGeneratorBuilder) {
        frameworkFixtureGeneratorBuilder.addCustomGenerator(
            className,
            NULLABLE_GENERATOR_PREFIX,
            framework.generateFixtureGenerator().rootSectionBuilder,
        )
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
        templateDiagnostic.mandatoryIsNotUsed(row)

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
