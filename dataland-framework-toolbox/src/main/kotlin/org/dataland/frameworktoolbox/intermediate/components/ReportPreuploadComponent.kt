package org.dataland.frameworktoolbox.intermediate.components

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.getBackendClientTypeReference
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

/**
 * A class ReferencedReportsComponent represents a set of pre-uploaded documents or reports
 */
class ReportPreuploadComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {
    private val companyReportType = TypeReference("org.dataland.datalandbackend.model.documents.CompanyReport", false)

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "Map", isNullable,
                listOf(
                    TypeReference("String", false),
                    companyReportType,
                ),
            ),
            listOf(
                Annotation(
                    fullyQualifiedName = "io.swagger.v3.oas.annotations.media.Schema",
                    rawParameterSpec = "example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE",
                    applicationTargetPrefix = "field",
                    additionalImports = setOf("org.dataland.datalandbackend.utils.JsonExampleFormattingConstants"),
                ),
                ValidAnnotation,
            ),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "org.dataland.datalandqaservice.model.reports.QaReportDataPoint",
                true,
                listOf(
                    TypeReference(
                        "Map", false,
                        listOf(
                            TypeReference("String", false),
                            companyReportType.getBackendClientTypeReference(),
                        ),
                    ),
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
