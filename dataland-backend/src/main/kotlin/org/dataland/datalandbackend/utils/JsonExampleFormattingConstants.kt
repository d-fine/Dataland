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

    private const val procurementCategory = "{" +
        "\"procuredProductTypesAndServicesNaceCodes\": [\"string\"]," +
        "\"numberOfSuppliersPerCountryCode\": {" +
        "\"DE\": null," +
        "\"GB\": 2" +
        "}," +
        "\"percentageOfTotalProcurement\": 0" +
        "}"

    const val PROCUREMENT_CATEGORIES_DEFAULT_VALUE: String = "{" +
        "\"Products\": $procurementCategory," +
        "\"Services\": $procurementCategory," +
        "\"RawMaterials\": $procurementCategory" +
        "}"

    const val SUBSTANTIAL_CONTRIBUTION_CRITIREA = "{" +
            "\"ClimateMitigation\": 20.0," +
            "\"ClimateAdaptation\": 20.0," +
            "\"Water\": 20.0," +
            "\"CircularEconomy\": 20.0," +
            "\"PollutionPrevention\": 20.0," +
            "\"Biodiversity\": 20.0" +
        "}"

    const val DNSH_CRITIREA = "{" +
            "\"ClimateMitigation\": \"Yes\"," +
            "\"ClimateAdaptation\": \"Yes\"," +
            "\"Water\": \"No\"," +
            "\"CircularEconomy\": \"Yes\"," +
            "\"PollutionPrevention\": \"Yes\"," +
            "\"Biodiversity\": \"No\"" +
        "}"
}
