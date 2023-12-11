package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.DataPointMaximumValue
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Social and employee matters" belonging to the category "Social" of the sfdr framework.
*/
data class SfdrSocialSocialAndEmployeeMatters(
    @field:Valid
    val humanRightsLegalProceedings: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val iloCoreLabourStandards: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val environmentalPolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val corruptionLegalProceedings: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val transparencyDisclosurePolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val humanRightsDueDiligencePolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val policyAgainstChildLabour: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val policyAgainstForcedLabour: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val policyAgainstDiscriminationInTheWorkplace: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val iso14001Certificate: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val policyAgainstBriberyAndCorruption: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val fairBusinessMarketingAdvertisingPolicy: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val technologiesExpertiseTransferPolicy: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val fairCompetitionPolicy: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val violationOfTaxRulesAndRegulation: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val unGlobalCompactPrinciplesCompliancePolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val averageGrossHourlyEarningsMaleEmployees: CurrencyDataPoint? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val averageGrossHourlyEarningsFemaleEmployees: CurrencyDataPoint? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -100)
    @field:DataPointMaximumValue(maximumValue = 100)
    val unadjustedGenderPayGapInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val femaleBoardMembers: ExtendedDataPoint<Long>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val maleBoardMembers: ExtendedDataPoint<Long>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    @field:DataPointMaximumValue(maximumValue = 100)
    val boardGenderDiversityInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val controversialWeaponsExposure: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val workplaceAccidentPreventionPolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    @field:DataPointMaximumValue(maximumValue = 100)
    val rateOfAccidentsInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val workdaysLostInDays: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val supplierCodeOfConduct: BaseDataPoint<YesNo>? = null,

    @field:Valid
    val grievanceHandlingMechanism: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val whistleblowerProtectionPolicy: BaseDataPoint<YesNo>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val reportedIncidentsOfDiscrimination: ExtendedDataPoint<Long>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val sanctionedIncidentsOfDiscrimination: ExtendedDataPoint<Long>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val ceoToEmployeePayGapRatio: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = 0)
    val excessiveCeoPayRatioInPercent: ExtendedDataPoint<BigDecimal>? = null,
)
