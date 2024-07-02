package org.dataland.frameworktoolbox.specific.datamodel.elements

import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import kotlin.io.path.div

class ReferencedReportValidatorBuilder(
    override val parentPackage: PackageBuilder,
    val frameworkIdentifier: String,
    val dataModelClassName: String,
    val dataModelFullyQualifiedName: String,
    val referencedReportsPath: String,
    val extendedDocumentFileReferences: List<String>,
) : DataModelElement {

    override val name: String = "ReferencedReportsListValidator"

    val fullyQualifiedName: String
        get() = parentPackage.fullyQualifiedName + "." + name

    override fun build(into: DatalandRepository) {
        val classPath = into.backendKotlinSrc / "${fullyQualifiedName.replace(".", "/")}.kt"

        val freemarkerTemplate = FreeMarker.configuration.getTemplate(
            "/specific/datamodel/elements/ValidateReferencedReportsList.kt.ftl",
        )
        val writer = FileWriter(classPath.toFile())
        freemarkerTemplate.process(
            mapOf(
                "package" to parentPackage.fullyQualifiedName,
                "framework" to frameworkIdentifier,
                "dataModelFullyQualifiedName" to dataModelFullyQualifiedName,
                "dataModelClassName" to dataModelClassName,
                "referencedReportsPath" to referencedReportsPath,
                "extendedDocumentsFileReferences" to extendedDocumentFileReferences,
            ),
            writer,
        )
    }
}
