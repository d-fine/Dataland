package org.dataland.frameworktoolbox.template

import org.dataland.datalandspecification.specifications.CalculationRule
import org.dataland.frameworktoolbox.intermediate.Framework
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
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TemplateComponentBuilderTest {
    @TempDir
    lateinit var tempDir: Path

    private val dummyTemplateRow =
        TemplateRow(
            fieldIdentifier = "1",
            category = "The Category",
            subCategory = "The Sub-Category",
            fieldName = "The name of the field",
            combinedTooltip = "A super-duper helpful tooltip",
            component = "Date",
            options = "I need more options!",
            unit = "A Unit",
            documentSupport = TemplateDocumentSupport.Extended,
            dependency = "",
            showWhenValueIs = "",
            mandatoryField = TemplateYesNo.No,
            aliasExport = "Export Name",
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

    @Test
    fun `ensure that field calculation rules are added to generated components`() {
        val rowWithCalculation =
            dummyTemplateRow.copy(
                fieldCalculation = "\"Sum\": [sourceA,sourceB]",
            )
        val templateComponentBuilder = getComponentBuilderForRow(rowWithCalculation)
        val targetGroup = DemoComponentGroupApiImpl()

        templateComponentBuilder.build(targetGroup)

        val component =
            targetGroup
                .get<ComponentGroup>("theCategory")
                .get<ComponentGroup>("theSubCategory")
                .get<DateComponent>("theNameOfTheField")
        assertEquals(listOf(CalculationRule(listOf("sourceA", "sourceB"), "Sum")), component.calculationRules)
    }

    @Test
    fun `ensure that malformed field calculation rules fail during template component generation`() {
        val rowWithMalformedCalculation =
            dummyTemplateRow.copy(
                fieldCalculation = "not a rule",
            )
        val templateComponentBuilder = getComponentBuilderForRow(rowWithMalformedCalculation)
        val targetGroup = DemoComponentGroupApiImpl()

        assertFails {
            templateComponentBuilder.build(targetGroup)
        }
    }

    @Test
    fun `ensure that field calculation rules are emitted in generated data point type specifications`() {
        val rowWithCalculation =
            dummyTemplateRow.copy(
                fieldName = "Calculated field",
                documentSupport = TemplateDocumentSupport.None,
                fieldCalculation = "\"Sum\": [sourceA,sourceB]",
            )
        val rowWithoutCalculation =
            dummyTemplateRow.copy(
                fieldIdentifier = "2",
                fieldName = "Plain field",
                documentSupport = TemplateDocumentSupport.None,
                fieldCalculation = null,
            )
        val framework =
            Framework(
                identifier = "testFramework",
                label = "Test Framework",
                explanation = "Test framework",
                order = 1,
            )
        TemplateComponentBuilder(
            template = ExcelTemplate(mutableListOf(rowWithCalculation, rowWithoutCalculation)),
            componentFactories = componentFactories,
            generationUtils = ComponentGenerationUtils(),
        ).build(framework.root)
        val repository = createTemporaryDatalandRepositoryWithPlainDateBaseType()

        val specificationBuilder = framework.generateSpecifications(repository)

        assertEquals(
            listOf(CalculationRule(listOf("sourceA", "sourceB"), "Sum")),
            specificationBuilder.database.dataPointTypes
                .getValue("plainDateCalculatedField")
                .calculationRules,
        )
        assertEquals(
            emptyList<CalculationRule>(),
            specificationBuilder.database.dataPointTypes
                .getValue("plainDatePlainField")
                .calculationRules,
        )
    }

    private fun createTemporaryDatalandRepositoryWithPlainDateBaseType(): DatalandRepository {
        val repository = DatalandRepository(tempDir)
        val specificationPath = repository.specificationDatabasePath
        listOf("dataPointBaseTypes", "dataPointTypes", "frameworks", "translations")
            .forEach { Files.createDirectories(specificationPath.resolve(it)) }
        Files.writeString(
            specificationPath.resolve("dataPointBaseTypes").resolve("plainDate.json"),
            """
            {
              "id" : "plainDate",
              "name" : "Just a date",
              "businessDefinition" : "A date without a time-zone represented by a LocalDate.",
              "validatedBy" : "java.time.LocalDate",
              "schema" : "java.time.LocalDate",
              "example" : "2007-03-05"
            }
            """.trimIndent(),
        )
        return repository
    }
}
