package org.dataland.datalandbackend.utils

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExampleFormattingConstants {
    private const val taxonomyEligibilityActivity = "{" +
        "\"taxonomyEligibleActivity\": {" +
        "\"value\": 0," +
        "\"quality\": \"Audited\"," +
        "\"dataSource\": {" +
        "\"report\": \"string\"," +
        "\"page\": 0" +
        "}," +
        "\"comment\": \"string\"" +
        "}" +
        "}"

    const val ELIGIBILITY_KPIS_DEFAULT_VALUE: String = "{" +
        "\"CreditInstitution\": " +
        taxonomyEligibilityActivity +
        "," +
        "\"InsuranceOrReinsurance\": " +
        taxonomyEligibilityActivity +
        "," +
        "\"AssetManagement\": " +
        taxonomyEligibilityActivity +
        "," +
        "\"InvestmentFirm\": " +
        taxonomyEligibilityActivity +
        "}"
}
