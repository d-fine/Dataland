package org.dataland.datalandbackend.utils

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExampleFormattingConstants {
    const val DATA_POINT_CONTENT = "{" +
        "\"value\": 0," +
        "\"quality\": \"Audited\"," +
        "\"dataSource\": {" +
        "\"report\": \"string\"," +
        "\"page\": 0" +
        "}," +
        "\"comment\": \"string\"" +
        "}"
    const val ELIGIBILITY_KPIS_DEFAULT_VALUE: String = "{" +
        "\"CreditInstitution\": {" +
        "\"taxonomyEligibleActivity\": " + DATA_POINT_CONTENT +
        "}," +
        "\"InsuranceOrReinsurance\": {" +
        "\"taxonomyEligibleActivity\": " + DATA_POINT_CONTENT +
        "}," +
        "\"AssetManagement\": {" +
        "\"taxonomyEligibleActivity\": " + DATA_POINT_CONTENT +
        "}," +
        "\"InvestmentFirm\": {" +
        "\"taxonomyEligibleActivity\": " + DATA_POINT_CONTENT +
        "}" +
        "}"
}
