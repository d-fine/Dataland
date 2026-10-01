package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates all SFDR datasets and data points to match the reworked SFDR data model:
 * - renames 7 KPIs
 * - deletes 10 obsolete KPIs (incl. the applicableHighImpactClimateSectors map)
 * - moves fiscalYearEnd into a new "company" subgroup together with the new mainPcafSector/companyExchangeStatus
 *   fields
 * - moves enterpriseValueInEUR/totalRevenueInEUR into the new "financial" top-level category
 */
@Suppress("ClassName")
class V34__RenameDeleteAndAddSfdrFields : BaseJavaMigration() {
    companion object {
        /**
         * Data point type renames that are a pure name change (identical schema), so they can be applied with a
         * simple UPDATE on the data_point_items table.
         */
        val dataPointTypeRenameMap =
            mapOf(
                "extendedDecimalWaterConsumptionInCubicMeters" to "extendedDecimalWaterWithdrawalInCubicMeters",
                "extendedDecimalRelativeWaterUsageInCubicMetersPerMillionEURRevenue" to
                    "extendedDecimalWaterWithdrawalIntensityInCubicMetersPerMillionEURRevenue",
                "extendedEnumYesNoViolationOfTaxRulesAndRegulation" to
                    "extendedEnumYesNoViolationOfUngcPrinciplesAndOecdGuidelines",
                "extendedEnumYesNoReportedChildLabourIncidents" to "extendedEnumYesNoRiskOfChildLabourIncidents",
                "extendedEnumYesNoReportedForcedOrCompulsoryLabourIncidents" to
                    "extendedEnumYesNoRiskOfReportedForcedOrCompulsoryLabourIncidents",
                "extendedIntegerReportedConvictionsOfBriberyAndCorruption" to
                    "extendedIntegerNumberOfReportedConvictionsOfBriberyAndCorruption",
                "extendedEnumYesNoLandDegradationDesertificationSoilSealingExposure" to
                    "extendedEnumYesNoLandDegradationDesertificationSoilSealing",
            )

        /**
         * Data point types that are removed entirely from the SFDR framework.
         */
        val deletedDataPointTypes =
            listOf(
                "extendedCurrencyAverageGrossHourlyEarningsFemaleEmployees",
                "extendedCurrencyAverageGrossHourlyEarningsMaleEmployees",
                "extendedDecimalGhgIntensityScope4InTonnesPerMillionEURRevenue",
                "extendedDecimalScope4GhgEmissionsInTonnes",
                "extendedEnumYesNoHighlyBiodiverseGrasslandExposure",
                "extendedEnumYesNoIso14001Certificate",
                "extendedEnumYesNoSecuritiesNotCertifiedAsGreen",
                "extendedEnumYesNoTechnologiesExpertiseTransferPolicy",
                "extendedEnumYesNoTransparencyDisclosurePolicy",
                "plainSfdrHighImpactClimateSectorsApplicableHighImpactClimateSectors",
            )

        /**
         * Simple key renames within the social category, where the parent object does not move.
         */
        private val socialKeyRenames =
            mapOf(
                "socialAndEmployeeMatters" to mapOf("violationOfTaxRulesAndRegulation" to "violationOfUngcPrinciplesAndOecdGuidelines"),
                "humanRights" to
                    mapOf(
                        "reportedChildLabourIncidents" to "riskOfChildLabourIncidents",
                        "reportedForcedOrCompulsoryLabourIncidents" to "riskOfReportedForcedOrCompulsoryLabourIncidents",
                    ),
                "antiCorruptionAndAntiBribery" to
                    mapOf(
                        "reportedConvictionsOfBriberyAndCorruption" to "numberOfReportedConvictionsOfBriberyAndCorruption",
                    ),
            )

        private val socialKeyDeletions =
            mapOf(
                "socialAndEmployeeMatters" to
                    listOf(
                        "averageGrossHourlyEarningsFemaleEmployees",
                        "averageGrossHourlyEarningsMaleEmployees",
                        "iso14001Certificate",
                        "technologiesExpertiseTransferPolicy",
                        "transparencyDisclosurePolicy",
                    ),
            )
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(context, "sfdr", this::migrateSfdrData)
        renameDataPointTypes(context)
        deleteDataPointTypes(context)
    }

    /**
     * Applies all renames, deletions, and restructuring to a single SFDR dataset blob.
     */
    fun migrateSfdrData(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        moveFiscalYearEndAndAddCompanyGroup(dataset)
        moveFinancialFieldsToNewGroup(dataset)
        renameEnvironmentalFields(dataset)
        deleteEnvironmentalFields(dataset)
        renameAndDeleteSocialFields(dataset)

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    /**
     * Moves general.general.fiscalYearEnd into a new general.company subgroup (alongside the new
     * mainPcafSector/companyExchangeStatus fields, which have no historical data and are left null).
     */
    private fun moveFiscalYearEndAndAddCompanyGroup(dataset: JSONObject) {
        val general = dataset.getOrJavaNull("general") as JSONObject? ?: return
        val generalGeneral = general.getOrJavaNull("general") as JSONObject? ?: return
        val fiscalYearEnd = generalGeneral.remove("fiscalYearEnd")

        val company = general.getOrJavaNull("company") as JSONObject? ?: JSONObject()
        company.put("fiscalYearEnd", fiscalYearEnd ?: JSONObject.NULL)
        general.put("company", company)
    }

    /**
     * Moves environmental.greenhouseGasEmissions.{enterpriseValueInEUR,totalRevenueInEUR} into the new top-level
     * financial category (financial.listedCompany.enterpriseValueInEUR / financial.financial.totalRevenueInEUR).
     */
    private fun moveFinancialFieldsToNewGroup(dataset: JSONObject) {
        val environmental = dataset.getOrJavaNull("environmental") as JSONObject? ?: return
        val greenhouseGasEmissions = environmental.getOrJavaNull("greenhouseGasEmissions") as JSONObject? ?: return

        val enterpriseValue = greenhouseGasEmissions.remove("enterpriseValueInEUR")
        val totalRevenue = greenhouseGasEmissions.remove("totalRevenueInEUR")

        val financial = dataset.getOrJavaNull("financial") as JSONObject? ?: JSONObject()
        val financialFinancial = financial.getOrJavaNull("financial") as JSONObject? ?: JSONObject()
        val financialListedCompany = financial.getOrJavaNull("listedCompany") as JSONObject? ?: JSONObject()

        financialFinancial.put("totalRevenueInEUR", totalRevenue ?: JSONObject.NULL)
        financialListedCompany.put("enterpriseValueInEUR", enterpriseValue ?: JSONObject.NULL)

        financial.put("financial", financialFinancial)
        financial.put("listedCompany", financialListedCompany)
        dataset.put("financial", financial)
    }

    /**
     * Renames the 3 renamed environmental fields in place (no restructuring needed).
     */
    private fun renameEnvironmentalFields(dataset: JSONObject) {
        val environmental = dataset.getOrJavaNull("environmental") as JSONObject? ?: return

        val biodiversity = environmental.getOrJavaNull("biodiversity") as JSONObject?
        biodiversity?.renameKey(
            "landDegradationDesertificationSoilSealingExposure",
            "landDegradationDesertificationSoilSealing",
        )

        val water = environmental.getOrJavaNull("water") as JSONObject?
        water?.renameKey("waterConsumptionInCubicMeters", "waterWithdrawalInCubicMeters")
        water?.renameKey(
            "relativeWaterUsageInCubicMetersPerMillionEURRevenue",
            "waterWithdrawalIntensityInCubicMetersPerMillionEURRevenue",
        )
    }

    /**
     * Deletes the obsolete environmental fields, including the applicableHighImpactClimateSectors map.
     */
    private fun deleteEnvironmentalFields(dataset: JSONObject) {
        val environmental = dataset.getOrJavaNull("environmental") as JSONObject? ?: return

        (environmental.getOrJavaNull("biodiversity") as JSONObject?)?.remove("highlyBiodiverseGrasslandExposure")
        (environmental.getOrJavaNull("greenhouseGasEmissions") as JSONObject?)?.apply {
            remove("scope4GhgEmissionsInTonnes")
            remove("ghgIntensityScope4InTonnesPerMillionEURRevenue")
        }
        (environmental.getOrJavaNull("energyPerformance") as JSONObject?)?.remove("applicableHighImpactClimateSectors")
    }

    /**
     * Applies all social category renames and deletions.
     */
    private fun renameAndDeleteSocialFields(dataset: JSONObject) {
        val social = dataset.getOrJavaNull("social") as JSONObject? ?: return

        for ((subcategory, renames) in socialKeyRenames) {
            val subcategoryObject = social.getOrJavaNull(subcategory) as JSONObject? ?: continue
            for ((oldKey, newKey) in renames) {
                subcategoryObject.renameKey(oldKey, newKey)
            }
        }

        for ((subcategory, keysToDelete) in socialKeyDeletions) {
            val subcategoryObject = social.getOrJavaNull(subcategory) as JSONObject? ?: continue
            keysToDelete.forEach { subcategoryObject.remove(it) }
        }

        social.remove("greenSecurities")
    }

    /**
     * Renames a key within a JSONObject in-place, preserving its value (including an explicit JSON null).
     */
    private fun JSONObject.renameKey(
        oldKey: String,
        newKey: String,
    ) {
        if (!this.has(oldKey)) return
        val value = if (this.isNull(oldKey)) JSONObject.NULL else this.get(oldKey)
        this.remove(oldKey)
        this.put(newKey, value)
    }

    /**
     * Renames the data point types of the 7 renamed KPIs in the data_point_items table. These are pure renames
     * (identical schema), so no value transformation is needed.
     */
    private fun renameDataPointTypes(context: Context?) {
        dataPointTypeRenameMap.forEach { (sourceType, targetType) ->
            val statement =
                context!!.connection.prepareStatement(
                    "UPDATE data_point_items SET data_point_type = ? WHERE data_point_type = ?",
                )
            statement.setString(1, targetType)
            statement.setString(2, sourceType)
            statement.executeUpdate()
            statement.close()
        }
    }

    /**
     * Deletes all data_point_items rows for the 10 obsolete data point types.
     */
    private fun deleteDataPointTypes(context: Context?) {
        deletedDataPointTypes.forEach { dataPointType ->
            val statement =
                context!!.connection.prepareStatement(
                    "DELETE FROM data_point_items WHERE data_point_type = ?",
                )
            statement.setString(1, dataPointType)
            statement.executeUpdate()
            statement.close()
        }
    }
}
