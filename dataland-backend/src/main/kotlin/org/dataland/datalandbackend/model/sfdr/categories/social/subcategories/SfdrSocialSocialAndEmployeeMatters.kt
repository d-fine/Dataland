package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.CurrencyDataPointValidation
import org.dataland.datalandbackend.validator.ExtendedNumberDataPointValidation
import org.dataland.datalandbackend.validator.PercentageDataPointValidation
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Social and employee matters" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialSocialAndEmployeeMatters(
    val humanRightsLegalProceedings: ExtendedDataPoint<YesNo>? = null,

    val iloCoreLabourStandards: ExtendedDataPoint<YesNo>? = null,

    val environmentalPolicy: BaseDataPoint<YesNo>? = null,

    val corruptionLegalProceedings: ExtendedDataPoint<YesNo>? = null,

    val transparencyDisclosurePolicy: BaseDataPoint<YesNo>? = null,

    val humanRightsDueDiligencePolicy: BaseDataPoint<YesNo>? = null,

    val policyAgainstChildLabour: BaseDataPoint<YesNo>? = null,

    val policyAgainstForcedLabour: BaseDataPoint<YesNo>? = null,

    val policyAgainstDiscriminationInTheWorkplace: BaseDataPoint<YesNo>? = null,

    val iso14001Certificate: BaseDataPoint<YesNo>? = null,

    val policyAgainstBriberyAndCorruption: BaseDataPoint<YesNo>? = null,

    val fairBusinessMarketingAdvertisingPolicy: ExtendedDataPoint<YesNo>? = null,

    val technologiesExpertiseTransferPolicy: ExtendedDataPoint<YesNo>? = null,

    val fairCompetitionPolicy: ExtendedDataPoint<YesNo>? = null,

    val violationOfTaxRulesAndRegulation: ExtendedDataPoint<YesNo>? = null,

    val unGlobalCompactPrinciplesCompliancePolicy: BaseDataPoint<YesNo>? = null,

    val oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: ExtendedDataPoint<YesNo>? = null,

    @field:CurrencyDataPointValidation
    val averageGrossHourlyEarningsMaleEmployees: CurrencyDataPoint? = null,

    @field:CurrencyDataPointValidation
    val averageGrossHourlyEarningsFemaleEmployees: CurrencyDataPoint? = null,

    @field:ExtendedNumberDataPointValidation
    val femaleBoardMembers: ExtendedDataPoint<Long>? = null,

    @field:ExtendedNumberDataPointValidation
    val maleBoardMembers: ExtendedDataPoint<Long>? = null,

    val controversialWeaponsExposure: ExtendedDataPoint<YesNo>? = null,

    val workplaceAccidentPreventionPolicy: BaseDataPoint<YesNo>? = null,

    @field:PercentageDataPointValidation
    val rateOfAccidentsInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:ExtendedNumberDataPointValidation
    val workdaysLostInDays: ExtendedDataPoint<BigDecimal>? = null,

    val supplierCodeOfConduct: BaseDataPoint<YesNo>? = null,

    val grievanceHandlingMechanism: ExtendedDataPoint<YesNo>? = null,
    @field:Valid
    val whistleblowerProtectionPolicy: BaseDataPoint<YesNo>? = null,

    @field:ExtendedNumberDataPointValidation
    val reportedIncidentsOfDiscrimination: ExtendedDataPoint<BigDecimal>? = null,

    @field:ExtendedNumberDataPointValidation
    val sanctionedIncidentsOfDiscrimination: ExtendedDataPoint<Long>? = null,

    @field:ExtendedNumberDataPointValidation
    val ceoToEmployeePayGapRatio: ExtendedDataPoint<BigDecimal>? = null,

)
