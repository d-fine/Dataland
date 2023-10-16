package org.dataland.datalandbackend.utils

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExampleFormattingConstants {
    private const val extendedDocumentReference = "{" +
        "\"page\": 0," +
        "\"tagName\": \"string\"," +
        "\"fileName\": \"string\"," +
        "\"fileReference\": \"string\"" +
        "}"

    private const val taxonomyEligibilityActivity = "{" +
        "\"taxonomyEligibleActivityInPercent\": {" +
        "\"value\": 0," +
        "\"quality\": \"Audited\"," +
        "\"dataSource\": $extendedDocumentReference," +
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
        "\"shareOfTotalProcurementInPercent\": 0" +
        "}"

    const val PROCUREMENT_CATEGORIES_DEFAULT_VALUE: String = "{" +
        "\"Products\": $procurementCategory," +
        "\"Services\": $procurementCategory," +
        "\"RawMaterials\": $procurementCategory" +
        "}"

    private const val extendedDataPointBigDecimal = "{" +
        "\"quality\": \"Audited\"," +
        "\"dataSource\": $extendedDocumentReference," +
        "\"comment\": \"string\"," +
        "\"value\": 0" +
        "}"

    const val HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE: String = "{" +
        "\"NaceCodeAInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeBInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeCInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeDInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeEInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeFInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeGInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeHInGWh\": $extendedDataPointBigDecimal," +
        "\"NaceCodeLInGWh\": $extendedDataPointBigDecimal" +
        "}"

    private const val companyReport = "{" +
        "\"fileReference\": \"string\"," +
        "\"fileName\": \"string\"," +
        "\"isGroupLevel\": \"Yes\"," +
        "\"reportDate\": \"2023-10-12\"," +
        "\"currency\": \"string\"" +
        "}"

    const val REFERENCED_REPORTS_DEFAULT_VALUE: String = "{" +
        "\"string\": $companyReport" +
        "}"
}
