package org.dataland.frameworktoolbox.specific.datamodel.elements

import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div

/**
 * A ReferencedReportValidatorBuilder is an in-memory Representation of a Kotlin class that
 * supports the generation of a validator for the referenced report list.
 * This validator checks whether all extendedDocumentReferences are also in the referencedReports stored in
 * referencedReportsPath for a dataClass specified by dataClass.
 * The validator will be placed in the parent package and the framework identifier is used to create the template.
 * Note: This builder only creates the validator class.
 * The annotation needs to be added to the dataClass to be effective.
 */
data class ReferencedReportValidatorBuilder(
    override val parentPackage: PackageBuilder,
    val dataClass: DataClassBuilder,
    val frameworkIdentifier: String,
    val referencedReportsPath: String,
    val extendedDocumentFileReferences: List<String>,
) : DataModelElement {

    override val name: String = "ReferencedReportsListValidator"

    override val empty: Boolean
        get() = extendedDocumentFileReferences.isEmpty()

    override val allNullable: Boolean
        get() = true

    val fullyQualifiedName: String
        get() = parentPackage.fullyQualifiedName + "." + name

    override fun build(into: Path) {
        val classPath = into / "${fullyQualifiedName.replace(".", "/")}.kt"

        val freemarkerTemplate = FreeMarker.configuration.getTemplate(
            "/specific/datamodel/elements/ValidateReferencedReportsList.kt.ftl",
        )
        val writer = FileWriter(classPath.toFile())
        freemarkerTemplate.process(
            mapOf(
                "package" to parentPackage.fullyQualifiedName,
                "framework" to frameworkIdentifier,
                "dataModelFullyQualifiedName" to dataClass.fullyQualifiedName,
                "dataModelClassName" to dataClass.name,
                "referencedReportsPath" to referencedReportsPath,
                "extendedDocumentsFileReferences" to extendedDocumentFileReferences,
            ),
            writer,
        )
    }
}
