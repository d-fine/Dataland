// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.vsme.model.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.frameworks.vsme.model.VsmeData
import org.dataland.datalandbackend.model.documents.CompanyReport
import kotlin.reflect.KClass

/**
* Annotation for the validation of referenced reports list for vsme
*/

@Constraint(validatedBy = [ReferencedReportsListConstraintValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class ReferencedReportsListValidator(
    val message: String = "The list of referenced reports is not complete. Please ensure that any file used as a " +
        "data source in the dataset is included in the list of referenced reports.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
* Class holding the validation logic for referenced reports list. It checks if the referenced reports list is complete
*/
class ReferencedReportsListConstraintValidator :
    ConstraintValidator<ReferencedReportsListValidator, VsmeData> {

    override fun isValid(dataset: VsmeData?, context: ConstraintValidatorContext?): Boolean {
        if (dataset == null) {
            return false
        }
        val referencedReportsMap = dataset.basic?.basisForPreparation?.referencedReports
        val referencedReportsFileReference = getFileReferencesFromReports(referencedReportsMap)

        val extendedDocumentsFileReferences = getExtendedDocumentReferences(dataset).filterNotNull()
        return extendedDocumentsFileReferences.all {
            referencedReportsFileReference.contains(it)
        }
    }

    private fun getFileReferencesFromReports(map: Map<String, CompanyReport>?): List<String> {
        if(map == null) return emptyList()
        val referencedReportsList = mutableListOf<String>()
            for (entry in map.entries) {
            referencedReportsList.add(entry.value.fileReference)
            }
        return referencedReportsList
    }

    @Suppress("MaxLineLength", "LongMethod")
    private fun getExtendedDocumentReferences(dataset: VsmeData): List<String?> {
        return listOf(
            dataset.basic?.energyAndGreenhousGasEmissions?.electricityTotalInMWh?.dataSource?.fileReference,
            dataset.basic?.energyAndGreenhousGasEmissions?.totalEmissionsInTonnesOfCO2Equivalents?.dataSource?.fileReference,
            dataset.basic?.energyAndGreenhousGasEmissions?.scope1EmissionsInTonnesOfCO2Equivalents?.dataSource?.fileReference,
            dataset.basic?.energyAndGreenhousGasEmissions?.scope2EmissionsInTonnesOfCO2Equivalents?.dataSource?.fileReference,
            dataset.basic?.energyAndGreenhousGasEmissions?.scope3EmissionsInTonnesOfCO2Equivalents?.dataSource?.fileReference,
            dataset.basic?.water?.waterWithdrawalAllSitesInCubicMeters?.dataSource?.fileReference,
        )
    }
}