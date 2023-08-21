package db.migration

import db.migration.utils.getCompanyAssociatedDatasetsForDataType
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing LkSG datasets to be in line again with the dataDictionary
 */
class V3__MigrateLksg : BaseJavaMigration() {

    private val mapOfOldToNewFieldNames = mapOf(
        "totalRevenue" to "annualTotalRevenue",
        "isContractProcessing" to "productionViaSubcontracting",
        "procurementCategories" to "productsServicesCategoriesPurchased",
        "adequateAndEffectiveRiskManagementSystem" to "riskManagementSystem",
        "riskManagementSystemFiscalYear" to "riskAnalysisInFiscalYear",
        "riskManagementSystemRisks" to "risksIdentified",
        "riskManagementSystemIdentifiedRisks" to "identifiedRisks",
        "riskManagementSystemCounteract" to "counteractingMeasures",
        "riskManagementSystemMeasures" to "whichCounteractingMeasures",
        "riskManagementSystemResponsibility" to "regulatedRiskManagementResponsibility",
        "grievanceHandlingMechanismUsedForReporting" to "grievanceHandlingReportingAccessible",
        "grievanceMechanismInformationProvided" to "appropriateGrievanceHandlingInformation",
        "grievanceMechanismSupportProvided" to "appropriateGrievanceHandlingSupport",
        "grievanceMechanismAccessToExpertise" to "accessToExpertiseForGrievanceHandling",
        "grievanceMechanismComplaints" to "grievanceComplaints",
        "grievanceMechanismComplaintsNumber" to "complaintsNumber",
        "grievanceMechanismComplaintsReason" to "complaintsReason",
        "grievanceMechanismComplaintsAction" to "actionsForComplaintsUndertaken",
        "grievanceMechanismComplaintsActionUndertaken" to "whichActionsForComplaintsUndertaken",
        "grievanceMechanismPublicAccess" to "publicAccessToGrievanceHandling",
        "grievanceMechanismProtection" to "whistleblowerProtection",
        "grievanceMechanismDueDiligenceProcess" to "dueDiligenceProcessForGrievanceHandling",
        "iso14000Certification" to "iso14001Certification",
        "humanRightsViolation" to "humanRightsViolationS",
        "employeeUnder18" to "employeeSUnder18",
        "employeeUnder15" to "employeeSUnder15",
        "employeeUnder18Apprentices" to "employeeSUnder18InApprenticeship",
        "employmentUnderLocalMinimumAgePrevention" to "measuresForPreventionOfEmploymentUnderLocalMinimumAge",
        "childLaborMeasures" to "additionalChildLaborMeasures",
        "adequateWageBeingWithheld" to "adequateWagesMeasures",
        "oshPolicyDisasterBehaviouralResponse" to "oshPolicyDisasterBehavioralResponse",
        "oshPolicyAccidentsBehaviouralResponse" to "oshPolicyAccidentsBehavioralResponse",
        "workplaceAccidentsUnder10" to "under10WorkplaceAccidents",
        "representedEmployees" to "employeeRepresentation",
        "harmfulSoilChange" to "harmfulSoilImpact",
        "soilBornDiseases" to "soilBorneDiseases",
        "soilSalinisation" to "soilSalinization",
        "fertilisersOrPollutants" to "fertilizersOrPollutants",
        "hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein" to
            "hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein",
        "hazardousAndOtherWasteImport" to "hazardousWasteDisposalOtherWasteImport",
    )

    private val newFieldsWithUploadButtonIfYes = listOf("smetaSocialAuditConcept", "codeOfConduct", "policyStatement")

    private fun writeToTemporarySubcategoryObject(
        subcategoryObjectTmp: JSONObject,
        categoryKey: String,
        subcategoryKey: String,
        fieldKey: String,
        fieldValue: Any,
    ) {
        subcategoryObjectTmp.put(fieldKey, fieldValue)
        if (fieldKey in mapOfOldToNewFieldNames.keys) {
            subcategoryObjectTmp.put(mapOfOldToNewFieldNames.getValue(fieldKey), fieldValue)
            subcategoryObjectTmp.remove(fieldKey)
        }
        if (fieldKey in newFieldsWithUploadButtonIfYes) {
            val newBaseDataPointObject = JSONObject("{\"value\":$fieldValue}")
            subcategoryObjectTmp.put(fieldKey, newBaseDataPointObject)
        }
        if (categoryKey == "social" &&
            subcategoryKey == "childLabor" &&
            fieldKey == "worstFormsOfChildLaborProhibition"
        ) {
            subcategoryObjectTmp.put("worstFormsOfChildLabor", "Yes")
        }
    }

    private fun writeToTemporaryDataset(
        datasetTmp: JSONObject,
        categoryKey: String,
        categoryObject: JSONObject,
    ) {
        val categoryObjectTmp = JSONObject()
        val subcategories = categoryObject.keys()
        subcategories.forEach { subcategory ->
            val subcategoryObject = categoryObject.opt(subcategory) as JSONObject
            val subcategoryObjectTmp = JSONObject()
            val fields = subcategoryObject.keys()
            fields.forEach { field ->
                val dataToMigrate = subcategoryObject.opt(field)
                writeToTemporarySubcategoryObject(
                    subcategoryObjectTmp, categoryKey, subcategory, field, dataToMigrate,
                )
            }
            categoryObjectTmp.put(subcategory, subcategoryObjectTmp)
        }
        datasetTmp.put(categoryKey, categoryObjectTmp)
    }

    override fun migrate(context: Context?) {
        val companyAssociatedDatasets = getCompanyAssociatedDatasetsForDataType(context, "lksg")

        companyAssociatedDatasets.forEach {
            var dataset = JSONObject(it.companyAssociatedData.getString("data"))
            val datasetTmp = JSONObject()
            val categories = dataset.keys()
            categories.forEach { category ->
                val categoryObject = dataset.opt(category) as JSONObject
                writeToTemporaryDataset(datasetTmp, category, categoryObject)
            }
            dataset = datasetTmp
            it.companyAssociatedData.put("data", dataset.toString())
            context!!.connection.createStatement().execute(it.getWriteQuery())
        }
    }
}
