package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.PercentageComponent
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateDocumentSupport
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PercentageComponentFactoryTest {
    private val dummyTemplateRow =
        TemplateRow(
            fieldIdentifier = "1",
            category = "The Category",
            subCategory = "The Sub-Category",
            fieldName = "The name of the field",
            combinedTooltip = "A super-duper helpful tooltip",
            component = "Percentage",
            options = "",
            unit = "",
            documentSupport = TemplateDocumentSupport.Extended,
            dependency = "",
            showWhenValueIs = "",
            mandatoryField = TemplateYesNo.No,
            aliasExport = "Export Name",
        )

    private fun generateComponentForOptions(options: String): PercentageComponent {
        val factory = PercentageComponentFactory(TemplateDiagnostic(DiagnosticManager()))
        val componentGroup = DemoComponentGroupApiImpl()
        return factory.generateComponent(
            dummyTemplateRow.copy(options = options),
            ComponentGenerationUtils(),
            componentGroup,
        ) as PercentageComponent
    }

    @Test
    fun `test that empty options do not set the NoUpload option`() {
        assertFalse(PercentageComponentFactory.hasNoUploadOption(""))
    }

    @Test
    fun `test that the sole NoUpload option is detected`() {
        assertTrue(PercentageComponentFactory.hasNoUploadOption("NoUpload"))
    }

    @Test
    fun `test that the NoUpload option is detected within a list of options`() {
        assertTrue(PercentageComponentFactory.hasNoUploadOption("SomethingElse, NoUpload"))
    }

    @Test
    fun `test that the NoUpload option is propagated to the generated component`() {
        assertTrue(generateComponentForOptions("NoUpload").hasNoUpload)
    }

    @Test
    fun `test that a component generated without options does not have the NoUpload option`() {
        assertFalse(generateComponentForOptions("").hasNoUpload)
    }
}
