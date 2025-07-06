package org.dataland.datalandbackend.utils

object NonFinancialsMapping {
    val aliasMap =
        mapOf(
            "substantialContributionToClimateChangeMitigationInPercent" to "SC_TO_CCM_PCT",
            "substantialContributionToClimateChangeAdaptationInPercent" to "SC_TO_CCA_PCT",
            "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" to "SC_TO_WTR_PCT",
            "substantialContributionToTransitionToACircularEconomyInPercent" to "SC_TO_CE_PCT",
            "substantialContributionToPollutionPreventionAndControlInPercent" to "SC_TO_PPC_PCT",
            "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" to "SC_TO_BIO_PCT",
            "dnshToClimateChangeMitigation" to "DNSH_TO_CCM",
            "dnshToClimateChangeAdaptation" to "DNSH_TO_CCA",
            "dnshToSustainableUseAndProtectionOfWaterAndMarineResources" to "DNSH_TO_WTR",
            "dnshToTransitionToACircularEconomy" to "DNSH_TO_CE",
            "dnshToPollutionPreventionAndControl" to "DNSH_TO_PPC",
            "dnshToProtectionAndRestorationOfBiodiversityAndEcosystems" to "DNSH_TO_BIO",
            "relativeShareInPercent" to "PCT",
            "minimumSafeguards" to "MIN_SAFEGUARDS",
            "enablingActivity" to "ENABLING",
            "transitionalActivity" to "TRANSITIONAL",
            "activityName" to "ACTIVITY_NAME",
        )
}
