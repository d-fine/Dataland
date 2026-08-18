package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.datapoints.ExtendedDocumentSupport
import org.dataland.frameworktoolbox.intermediate.group.DemoComponentGroupApiImpl
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val NO_UPLOAD_QUALIFIER = "org.dataland.datalandbackend.validator.NoUpload"
private const val SCHEMA_QUALIFIER = "io.swagger.v3.oas.annotations.media.Schema"

class PercentageComponentTest {
    private fun generateDataModelAnnotations(hasNoUpload: Boolean): List<Annotation> {
        val componentGroup = DemoComponentGroupApiImpl()
        val component =
            componentGroup.create<PercentageComponent>("testPercentage") {
                documentSupport = ExtendedDocumentSupport
                this.hasNoUpload = hasNoUpload
            }
        val dataClassBuilder =
            PackageBuilder("test", null)
                .addClass("TestDataClass", "A data class used for testing")
        component.generateDefaultDataModel(dataClassBuilder)
        return dataClassBuilder.properties.single().annotations
    }

    @Test
    fun `test that the NoUpload annotation is added to the data model if the option is set`() {
        val annotations = generateDataModelAnnotations(hasNoUpload = true)

        val noUploadAnnotation = annotations.single { it.fullyQualifiedName == NO_UPLOAD_QUALIFIER }
        assertEquals("field", noUploadAnnotation.applicationTargetPrefix)
        assertTrue(annotations.any { it.fullyQualifiedName == SCHEMA_QUALIFIER })
    }

    @Test
    fun `test that no NoUpload annotation is added to the data model if the option is not set`() {
        val annotations = generateDataModelAnnotations(hasNoUpload = false)

        assertFalse(annotations.any { it.fullyQualifiedName == NO_UPLOAD_QUALIFIER })
        assertTrue(annotations.any { it.fullyQualifiedName == SCHEMA_QUALIFIER })
    }
}
