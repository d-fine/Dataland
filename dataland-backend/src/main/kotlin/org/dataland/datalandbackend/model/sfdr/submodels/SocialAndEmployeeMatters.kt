package org.dataland.datalandbackend.model.sfdr.submodels

import java.math.BigDecimal
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Social and employee matters"
 */
data class SocialAndEmployeeMatters(
    val humanRightsLegalProceedings: DataPoint<YesNo>?,

    val iloCoreLabourStandards: DataPoint<YesNo>?,

    val environmentalPolicy: DataPoint<YesNo>?,

    val corruptionLegalProceedings: DataPoint<YesNo>?,

    val transparencyDisclosurePolicy: DataPoint<YesNo>?,

    val humanRightsDueDiligencePolicy: DataPoint<YesNo>?,

    val childForcedDiscriminationPolicy: DataPoint<YesNo>?,

    val iso14001: DataPoint<YesNo>?,

    val briberyCorruptionPolicy: DataPoint<YesNo>?,

    val fairBusinessMarketingAdvertisingPolicy: DataPoint<YesNo>?,

    val technologiesExpertiseTransferPolicy: DataPoint<YesNo>?,

    val fairCompetitionPolicy: DataPoint<YesNo>?,

    val violationOfTaxRulesAndRegulation: DataPoint<YesNo>?,

    val unGlobalCompactPrinciplesCompliancePolicy: DataPoint<YesNo>?,

    val oecdGuidelinesForMultinationalEnterprisesPolicy: DataPoint<YesNo>?,

    val averageGrossHourlyEarningsMaleEmployees: DataPoint<BigDecimal>?,

    val averageGrossHourlyEarningsFemaleEmployees: DataPoint<BigDecimal>?,

    val femaleBoardMembers: DataPoint<BigDecimal>?,

    val maleBoardMembers: DataPoint<BigDecimal>?,

    val controversialWeaponsExposure: DataPoint<YesNo>?,

    val workplaceAccidentPreventionPolicy: DataPoint<YesNo>?,

    val rateOfAccidents: DataPoint<BigDecimal>?,

    val workdaysLost: DataPoint<BigDecimal>?,

    val supplierCodeOfConduct: DataPoint<YesNo>?,

    val grievanceHandlingMechanism: DataPoint<YesNo>?,

    val whistleblowerProtectionPolicy: DataPoint<YesNo>?,

    val reportedIncidentsOfDiscrimination: DataPoint<BigDecimal>?,

    val sanctionsIncidentsOfDiscrimination: DataPoint<BigDecimal>?,

    val ceoToEmployeePayGap: DataPoint<BigDecimal>?,
)
