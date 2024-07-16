package org.dataland.datalandbackend.model.eutaxonomy.financials

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
/**
 * Annotation Class holding the validation logic for referenced reports list.
 * It checks if the referenced reports list is complete
 */

@Constraint(validatedBy = [ReferencedReportsListValidatorForEutaxonomyFinancials::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class ValidateReferencedReportsListForEuTaxonomyFinancials(
    val message: String = "The list of referenced reports is not complete. Please ensure that any file used as a " +
        "data source in the dataset is included in the list of referenced reports.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for referenced reports list. It checks if the referenced reports list is complete
 */
class ReferencedReportsListValidatorForEutaxonomyFinancials :
    ConstraintValidator<ValidateReferencedReportsListForEuTaxonomyFinancials, EuTaxonomyDataForFinancials> {

    override fun isValid(dataset: EuTaxonomyDataForFinancials?, context: ConstraintValidatorContext?): Boolean {
        if (dataset == null) {
            return false
        }

        val referencedReportsMap = dataset.referencedReports
        val referencedReportsFileReference = getFileReferencesFromReports(referencedReportsMap)

        val extendedDocumentsFileReferences = mutableListOf<String>()
        collectFileReferences(dataset.investmentFirmKpis, extendedDocumentsFileReferences)
        collectFileReferences(dataset.creditInstitutionKpis, extendedDocumentsFileReferences)
        collectFileReferences(dataset.eligibilityKpis, extendedDocumentsFileReferences)
        collectFileReferences(dataset.insuranceKpis, extendedDocumentsFileReferences)
        collectFileReferences(dataset.assurance, extendedDocumentsFileReferences)

        return extendedDocumentsFileReferences.all {
            referencedReportsFileReference.contains(it)
        }
    }
    private fun getFileReferencesFromReports(map: Map<String, CompanyReport>?): List<String> {
        if (map == null) return emptyList()
        val referencedReportsList = mutableListOf<String>()
        for (entry in map.entries) {
            referencedReportsList.add(entry.value.fileReference)
        }
        return referencedReportsList
    }
    private fun collectFileReferences(data: Any?, fileReferences: MutableList<String>) {
        if (data == null) return
        when (data) {
            is ExtendedDocumentReference -> {
                fileReferences.add(data.fileReference)
            }
            is Map<*, *> -> {
                data.values.forEach { value ->
                    collectFileReferences(value, fileReferences)
                }
            }
            else -> {
                if (data::class.isData) {
                    val properties = data::class.declaredMemberProperties
                    for (property in properties) {
                        val value = (property as KCallable<*>).call(data)
                        collectFileReferences(value, fileReferences)
                    }
                }
            }
        }
    }
}
