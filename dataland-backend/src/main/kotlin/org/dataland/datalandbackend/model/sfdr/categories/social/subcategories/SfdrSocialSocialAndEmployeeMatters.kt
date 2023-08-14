package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Social and employee matters" belonging to the category "Social" of the sfdr framework.
 */
data class SfdrSocialSocialAndEmployeeMatters(
    val humanRightsLegalProceedings: DataPointOneValue<YesNo>? = null,

    val iloCoreLabourStandards: DataPointOneValue<YesNo>? = null,

    val environmentalPolicy: DataPointOneValue<YesNo>? = null,

    val corruptionLegalProceedings: DataPointOneValue<YesNo>? = null,

    val transparencyDisclosurePolicy: DataPointOneValue<YesNo>? = null,

    val humanRightsDueDiligencePolicy: DataPointOneValue<YesNo>? = null,

    val childForcedDiscriminationPolicy: DataPointOneValue<YesNo>? = null,

    val iso14001Certificate: DataPointOneValue<YesNo>? = null,

    val briberyCorruptionPolicy: DataPointOneValue<YesNo>? = null,

    val fairBusinessMarketingAdvertisingPolicy: DataPointOneValue<YesNo>? = null,

    val technologiesExpertiseTransferPolicy: DataPointOneValue<YesNo>? = null,

    val fairCompetitionPolicy: DataPointOneValue<YesNo>? = null,

    val violationOfTaxRulesAndRegulation: DataPointOneValue<YesNo>? = null,

    val unGlobalCompactPrinciplesCompliancePolicy: DataPointOneValue<YesNo>? = null,

    val oecdGuidelinesForMultinationalEnterprisesPolicy: DataPointOneValue<YesNo>? = null,

    val averageGrossHourlyEarningsMaleEmployees: DataPointWithUnit<BigDecimal>? = null,

    val averageGrossHourlyEarningsFemaleEmployees: DataPointWithUnit<BigDecimal>? = null,

    val femaleBoardMembers: DataPointWithUnit<BigDecimal>? = null,

    val maleBoardMembers: DataPointWithUnit<BigDecimal>? = null,

    val controversialWeaponsExposure: DataPointOneValue<YesNo>? = null,

    val workplaceAccidentPreventionPolicy: DataPointOneValue<YesNo>? = null,

    val rateOfAccidents: DataPointWithUnit<BigDecimal>? = null,

    val workdaysLost: DataPointWithUnit<BigDecimal>? = null,

    val supplierCodeOfConduct: DataPointOneValue<YesNo>? = null,

    val grievanceHandlingMechanism: DataPointOneValue<YesNo>? = null,

    val whistleblowerProtectionPolicy: DataPointOneValue<YesNo>? = null,

    val reportedIncidentsOfDiscrimination: DataPointWithUnit<BigDecimal>? = null,

    val sanctionsIncidentsOfDiscrimination: DataPointWithUnit<BigDecimal>? = null,

    val ceoToEmployeePayGap: DataPointWithUnit<BigDecimal>? = null,
)
