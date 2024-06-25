package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import kotlin.reflect.KClass

@Constraint(validatedBy = [SfdrDataValidator::class])
@Target(AnnotationTarget.CLASS)
annotation class ReferencedReportsListIsComplete(
    // todo rename Annotation (specific for Sfdr)
    val message: String = "The list of referenced reports is not complete.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class SfdrDataValidator : ConstraintValidator<ReferencedReportsListIsComplete, SfdrData> {
    // todo add this annotation in the toolbox
    override fun isValid(value: SfdrData?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return false
        }
        val referencedReports = getAllReferencedReports(value)
        val referencedReportsFileReference = getFileReferencesFromReports(referencedReports)
        val extendedDocumentReferences = getAllExtendedDocumentsReference(value)
        return extendedDocumentReferences.any {
            !referencedReportsFileReference.contains(it.fileReference)
            // todo discuss: check for more than just existence of file reference? maybe name (i.e. it.name == key of map)?
        }
    }
    private fun getFileReferencesFromReports(map: Map<String, CompanyReport>?): List<String> {
        val referencedReportsList = mutableListOf<String>()
        for (entry in map?.entries!!) {
            referencedReportsList.add(entry.value.fileReference)
        }
        return referencedReportsList
    }

    private fun getAllReferencedReports(value: SfdrData): Map<String, CompanyReport>? {
        return value.general.general.referencedReports
    }

    private fun getAllExtendedDocumentsReference(sfdrData: SfdrData): List<ExtendedDocumentReference> {
        // todo implement
        return emptyList()
    }
}
