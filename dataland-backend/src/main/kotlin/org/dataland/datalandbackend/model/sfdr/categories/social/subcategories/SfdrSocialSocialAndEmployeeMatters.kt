package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Social and employee matters" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialSocialAndEmployeeMatters(
    val humanRightsLegalProceedings: ExtendedDataPoint<YesNo>? = null,

    val iloCoreLabourStandards: ExtendedDataPoint<YesNo>? = null,

    val environmentalPolicy: ExtendedDataPoint<YesNo>? = null,

    val corruptionLegalProceedings: ExtendedDataPoint<YesNo>? = null,

    val transparencyDisclosurePolicy: ExtendedDataPoint<YesNo>? = null,

    val humanRightsDueDiligencePolicy: ExtendedDataPoint<YesNo>? = null,

    val policyAgainstChildLabour: ExtendedDataPoint<YesNo>? = null,

    val policyAgainstForcedLabour: ExtendedDataPoint<YesNo>? = null,

    val policyAgainstDiscriminationInTheWorkplace: ExtendedDataPoint<YesNo>? = null,

    val iso14001Certificate: ExtendedDataPoint<YesNo>? = null,

    val policyAgainstBriberyAndCorruption: ExtendedDataPoint<YesNo>? = null,

    val fairBusinessMarketingAdvertisingPolicy: ExtendedDataPoint<YesNo>? = null,

    val technologiesExpertiseTransferPolicy: ExtendedDataPoint<YesNo>? = null,

    val fairCompetitionPolicy: ExtendedDataPoint<YesNo>? = null,

    val violationOfTaxRulesAndRegulation: ExtendedDataPoint<YesNo>? = null,

    val unGlobalCompactPrinciplesCompliancePolicy: ExtendedDataPoint<YesNo>? = null,

    val oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: ExtendedDataPoint<YesNo>? = null,

    val averageGrossHourlyEarningsMaleEmployees: CurrencyDataPoint? = null,

    val averageGrossHourlyEarningsFemaleEmployees: CurrencyDataPoint? = null,

    val femaleBoardMembers: ExtendedDataPoint<BigDecimal>? = null,

    val maleBoardMembers: ExtendedDataPoint<BigDecimal>? = null,

    val controversialWeaponsExposure: ExtendedDataPoint<YesNo>? = null,

    val workplaceAccidentPreventionPolicy: ExtendedDataPoint<YesNo>? = null,

    val rateOfAccidentsInPercent: ExtendedDataPoint<BigDecimal>? = null,

    val workdaysLostInDays: ExtendedDataPoint<BigDecimal>? = null,

    val supplierCodeOfConduct: ExtendedDataPoint<YesNo>? = null,

    val grievanceHandlingMechanism: ExtendedDataPoint<YesNo>? = null,

    val whistleblowerProtectionPolicy: ExtendedDataPoint<YesNo>? = null,

    val reportedIncidentsOfDiscrimination: ExtendedDataPoint<BigDecimal>? = null,

    val sanctionedIncidentsOfDiscrimination: ExtendedDataPoint<BigDecimal>? = null,

    val ceoToEmployeePayGapRatio: ExtendedDataPoint<BigDecimal>? = null,
)
