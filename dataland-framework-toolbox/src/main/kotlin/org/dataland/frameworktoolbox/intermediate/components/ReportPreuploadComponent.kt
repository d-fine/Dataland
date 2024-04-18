package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * A class ReferencedReportsComponent represents a set of pre-uploaded documents or reports
 */
class ReportPreuploadComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "Map", isNullable,
                listOf(
                    TypeReference("String", false),
                    TypeReference("org.dataland.datalandbackend.model.documents.CompanyReport", false),
                ),
            ),
            listOf(
                Annotation(
                    fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                    rawParameterSpec = "implementation = Map::class,\n" +
                        "example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE",
                    applicationTargetPrefix = "field",
                    additionalImports = setOf("org.dataland.datalandbackend.utils.JsonExampleFormattingConstants"),
                ),
            ),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        // Component not displayed on view page
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        uploadCategoryBuilder.addStandardUploadConfigCell(
            component = this,
            uploadComponentName = "UploadReports",
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        sectionBuilder.addAtomicExpression(
            identifier,
            "dataGenerator.reports",
        )
    }
}
