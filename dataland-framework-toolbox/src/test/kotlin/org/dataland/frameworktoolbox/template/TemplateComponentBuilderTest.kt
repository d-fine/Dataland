package org.dataland.frameworktoolbox.template

import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.intermediate.group.get
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.DateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateDocumentSupport
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TemplateComponentBuilderTest {
    private val dummyTemplateRow =
        TemplateRow(
            fieldIdentifier = "1",
            category = "The Category",
            subCategory = "The Sub-Category",
            fieldName = "The name of the field",
            combinedTooltip = "A super-duper helpfull tooltip",
            component = "Date",
            options = "I need more options!",
            unit = "A Unit",
            documentSupport = TemplateDocumentSupport.Extended,
            dependency = "",
            showWhenValueIs = "",
            mandatoryField = TemplateYesNo.No,
        )
    private val diagnosticManager = DiagnosticManager()
    private val componentFactories = listOf(DateComponentFactory(TemplateDiagnostic(diagnosticManager)))

    private fun getComponentBuilderForRow(row: TemplateRow): TemplateComponentBuilder =
        TemplateComponentBuilder(
            template = ExcelTemplate(mutableListOf(row)),
            componentFactories = componentFactories,
            generationUtils = ComponentGenerationUtils(),
        )

    @Test
    fun `ensure that fields can be created with a category and a subcategory`() {
        val rowWithoutCategory = dummyTemplateRow.copy()
        val templateComponentBuilder = getComponentBuilderForRow(rowWithoutCategory)
        val targetGroup = DemoComponentGroupApiImpl()

        templateComponentBuilder.build(targetGroup)

        val category = targetGroup.get<ComponentGroup>("theCategory")
        val subcategory = category.get<ComponentGroup>("theSubCategory")
        subcategory.get<DateComponent>("theNameOfTheField")
    }

    @Test
    fun `ensure that the category can be left empty to generate top level components`() {
        val rowWithoutCategory = dummyTemplateRow.copy(category = "", subCategory = "")
        val templateComponentBuilder = getComponentBuilderForRow(rowWithoutCategory)
        val targetGroup = DemoComponentGroupApiImpl()

        templateComponentBuilder.build(targetGroup)

        assertTrue(targetGroup.getOrNull<DateComponent>("theNameOfTheField") is DateComponent)
    }

    @Test
    fun `ensure that the subcategory can be left empty`() {
        val rowWithoutCategory = dummyTemplateRow.copy(subCategory = "")
        val templateComponentBuilder = getComponentBuilderForRow(rowWithoutCategory)
        val targetGroup = DemoComponentGroupApiImpl()

        templateComponentBuilder.build(targetGroup)

        val containingGroup = targetGroup.get<ComponentGroup>("theCategory")
        containingGroup.get<DateComponent>("theNameOfTheField")
    }

    @Test
    fun `ensure that an error is thrown if the category is left empty but the subcategory isnt`() {
        val rowWithoutCategory = dummyTemplateRow.copy(category = "", subCategory = "Hi")
        val templateComponentBuilder = getComponentBuilderForRow(rowWithoutCategory)
        val targetGroup = DemoComponentGroupApiImpl()

        assertFails {
            templateComponentBuilder.build(targetGroup)
        }
    }

    @Test
    fun `ensure that an error is thrown if a row is specified that cannot be processed`() {
        val rowWithoutCategory = dummyTemplateRow.copy(component = "Unknown component")
        val templateComponentBuilder = getComponentBuilderForRow(rowWithoutCategory)
        val targetGroup = DemoComponentGroupApiImpl()

        assertFails {
            templateComponentBuilder.build(targetGroup)
        }
    }
}
