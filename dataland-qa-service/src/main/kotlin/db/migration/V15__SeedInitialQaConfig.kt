package db.migration

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates the singleton qa_config table (plus its Hibernate Envers audit table) and seeds it with the initial
 * pre-approval configuration, migrating the values that were previously hardcoded/environment-configured:
 * the SFDR exempt fields (previously in application.yml), the significance check thresholds (previously
 * constants in SignificanceCheckService), and the auto-preapproval flag (previously a Spring @Value property).
 *
 * The qa_config table is guaranteed by this migration to always contain exactly one row.
 */
@Suppress("ClassName")
class V15__SeedInitialQaConfig : BaseJavaMigration() {
    companion object {
        const val QA_CONFIG_TABLE = "qa_config"
        const val QA_CONFIG_AUD_TABLE = "qa_config_aud"
        const val REVINFO_TABLE = "revinfo"

        private val sfdrExemptFields =
            setOf(
                "extendedIntegerCasesOfInsufficientActionAgainstBriberyAndCorruption",
                "extendedEnumYesNoPrimaryForestAndWoodedLandOfNativeSpeciesExposure",
                "extendedDecimalScope3UpstreamGhgEmissionsInTonnes",
                "extendedDecimalScope3DownstreamGhgEmissionsInTonnes",
                "extendedDecimalRateOfAccidents",
                "extendedDecimalEmissionsOfAirPollutantsInTonnes",
                "extendedEnumYesNoLandDegradationDesertificationSoilSealingExposure",
                "extendedDecimalWaterConsumptionInCubicMeters",
                "extendedDecimalEmissionsOfInorganicPollutantsInTonnes",
                "extendedDecimalWorkdaysLostInDays",
                "extendedEnumYesNoThreatenedSpeciesExposure",
                "extendedEnumYesNoUnGlobalCompactPrinciplesCompliancePolicy",
                "extendedEnumYesNoProtectedAreasExposure",
                "extendedDecimalEmissionsToWaterInTonnes",
                "extendedEnumYesNoFossilFuelSectorExposure",
                "extendedEnumYesNoViolationOfTaxRulesAndRegulation",
                "extendedEnumYesNoGrievanceHandlingMechanism",
                "extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption",
                "extendedEnumYesNoIloCoreLabourStandards",
                "extendedEnumYesNoControversialWeaponsExposure",
                "extendedDecimalEmissionsOfOzoneDepletionSubstancesInTonnes",
                "extendedEnumYesNoHumanRightsLegalProceedings",
                "extendedEnumYesNoManufactureOfAgrochemicalPesticidesProducts",
                "extendedIntegerNumberOfReportedIncidentsOfHumanRightsViolations",
                "extendedEnumYesNoRareOrEndangeredEcosystemsExposure",
                "extendedEnumYesNoSupplierCodeOfConduct",
                "extendedEnumYesNoBiodiversityProtectionPolicy",
                "extendedEnumYesNoHumanRightsDueDiligencePolicy",
                "extendedEnumYesNoOecdGuidelinesForMultinationalEnterprisesGrievanceHandling",
                "extendedDecimalTotalRevenueInEUR",
                "extendedDecimalRenewableEnergyProductionInGWh",
                "extendedIntegerMaleBoardMembersBoardOfDirectors",
                "extendedDecimalUnadjustedGenderPayGapInPercent",
                "extendedIntegerFemaleBoardMembersSupervisoryBoard",
                "extendedIntegerMaleBoardMembersSupervisoryBoard",
                "extendedDecimalNonRenewableEnergyConsumptionInGWh",
                "extendedIntegerFemaleBoardMembersBoardOfDirectors",
                "extendedDecimalHazardousAndRadioactiveWasteInTonnes",
                "extendedDecimalTotalHighImpactClimateSectorEnergyConsumptionInGWh",
            )

        /**
         * The initial pre-approval configuration seeded by this migration.
         */
        val initialConfig =
            PreApprovalConfig(
                exemptFields = mapOf(DataTypeEnum.sfdr to sfdrExemptFields),
                samplingProbability = 0.0,
                decimalRelativeThreshold = 0.5,
                integerAbsoluteThreshold = 5,
                individualDecimalThresholds = emptyMap(),
                individualIntegerThresholds = emptyMap(),
                autoPreApprovalEnabled = true,
                submitUserId = null,
            )
    }

    override fun migrate(context: Context) {
        val connection = context.connection
        val metaData = connection.metaData

        if (!metaData.getTables(null, null, REVINFO_TABLE, null).next()) {
            connection.createStatement().execute(
                """
                CREATE TABLE $REVINFO_TABLE (
                    rev SERIAL PRIMARY KEY,
                    revtstmp BIGINT
                )
                """.trimIndent(),
            )
        }

        if (!metaData.getTables(null, null, QA_CONFIG_TABLE, null).next()) {
            connection.createStatement().execute(
                """
                CREATE TABLE $QA_CONFIG_TABLE (
                    id UUID PRIMARY KEY,
                    config TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }

        if (!metaData.getTables(null, null, QA_CONFIG_AUD_TABLE, null).next()) {
            connection.createStatement().execute(
                """
                CREATE TABLE $QA_CONFIG_AUD_TABLE (
                    id UUID NOT NULL,
                    rev INTEGER NOT NULL,
                    revtype SMALLINT,
                    config TEXT,
                    PRIMARY KEY (id, rev),
                    CONSTRAINT fk_qa_config_aud_revinfo FOREIGN KEY (rev) REFERENCES $REVINFO_TABLE (rev)
                )
                """.trimIndent(),
            )
        }

        val hasRow =
            connection
                .createStatement()
                .executeQuery("SELECT COUNT(*) FROM $QA_CONFIG_TABLE")
                .let {
                    it.next()
                    it.getInt(1) > 0
                }
        if (hasRow) {
            return
        }

        val configJson = JsonUtils.defaultObjectMapper.writeValueAsString(initialConfig)
        val statement =
            connection.prepareStatement(
                "INSERT INTO $QA_CONFIG_TABLE (id, config) VALUES (?, ?)",
            )
        statement.setObject(1, QaConfigEntity.QA_CONFIG_SINGLETON_ID)
        statement.setString(2, configJson)
        statement.executeUpdate()
        statement.close()
    }
}
