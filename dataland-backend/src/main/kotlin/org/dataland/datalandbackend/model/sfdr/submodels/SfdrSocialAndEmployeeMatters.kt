package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Social and employee matters"
 */
data class SfdrSocialAndEmployeeMatters(
    val humanRightsLegalProceedings: DataPoint<YesNo>? = null,

    val iloCoreLabourStandards: DataPoint<YesNo>? = null,

    val environmentalPolicy: DataPoint<YesNo>? = null,

    val corruptionLegalProceedings: DataPoint<YesNo>? = null,

    val transparencyDisclosurePolicy: DataPoint<YesNo>? = null,

    val humanRightsDueDiligencePolicy: DataPoint<YesNo>? = null,

    val childForcedDiscriminationPolicy: DataPoint<YesNo>? = null,

    val iso14001: DataPoint<YesNo>? = null,

    val briberyCorruptionPolicy: DataPoint<YesNo>? = null,

    val fairBusinessMarketingAdvertisingPolicy: DataPoint<YesNo>? = null,

    val technologiesExpertiseTransferPolicy: DataPoint<YesNo>? = null,

    val fairCompetitionPolicy: DataPoint<YesNo>? = null,

    val violationOfTaxRulesAndRegulation: DataPoint<YesNo>? = null,

    val unGlobalCompactPrinciplesCompliancePolicy: DataPoint<YesNo>? = null,

    val oecdGuidelinesForMultinationalEnterprisesPolicy: DataPoint<YesNo>? = null,

    val averageGrossHourlyEarningsMaleEmployees: DataPoint<BigDecimal>? = null,

    val averageGrossHourlyEarningsFemaleEmployees: DataPoint<BigDecimal>? = null,

    val femaleBoardMembers: DataPoint<BigDecimal>? = null,

    val maleBoardMembers: DataPoint<BigDecimal>? = null,

    val controversialWeaponsExposure: DataPoint<YesNo>? = null,

    val workplaceAccidentPreventionPolicy: DataPoint<YesNo>? = null,

    val rateOfAccidents: DataPoint<BigDecimal>? = null,

    val workdaysLost: DataPoint<BigDecimal>? = null,

    val supplierCodeOfConduct: DataPoint<YesNo>? = null,

    val grievanceHandlingMechanism: DataPoint<YesNo>? = null,

    val whistleblowerProtectionPolicy: DataPoint<YesNo>? = null,

    val reportedIncidentsOfDiscrimination: DataPoint<BigDecimal>? = null,

    val sanctionsIncidentsOfDiscrimination: DataPoint<BigDecimal>? = null,

    val ceoToEmployeePayGap: DataPoint<BigDecimal>? = null,
)
