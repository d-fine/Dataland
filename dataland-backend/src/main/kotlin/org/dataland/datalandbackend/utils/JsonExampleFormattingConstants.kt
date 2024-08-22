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

    const val SUBCONTRACTING_COMPANIES_DEFAULT_VALUE = "{" +
        "\"DE\": [\"NaceCodeA, NaceCodeB\"]," +
        "\"GB\": [\"NaceCodeC\"]" +
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

    private const val driveMix = "{" +
        "\"driveMixPerFleetSegmentInPercent\": 0," +
        "\"totalAmountOfVehicles\": 0" +
        "}"

    const val DRIVE_MIX_DEFAULT_VALUE: String = "{" +
        "\"SmallTrucks\": $driveMix," +
        "\"MediumTrucks\": $driveMix," +
        "\"LargeTrucks\": $driveMix" +
        "}"

    private const val extendedDataPointBigDecimal = "{" +
        "\"quality\": \"Audited\"," +
        "\"dataSource\": $extendedDocumentReference," +
        "\"comment\": \"string\"," +
        "\"value\": 0" +
        "}"

    private const val highImpactClimateSectorEnergyConsumption = "{" +
        "\"highImpactClimateSectorEnergyConsumptionInGWh\": $extendedDataPointBigDecimal," +
        "\"highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue\": $extendedDataPointBigDecimal" +
        "}"

    const val HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE: String = "{" +
        "\"NaceCodeA\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeB\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeC\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeD\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeE\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeF\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeG\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeH\": $highImpactClimateSectorEnergyConsumption," +
        "\"NaceCodeL\": $highImpactClimateSectorEnergyConsumption" +
        "}"

    private const val companyReport = "{" +
        "\"fileReference\": \"string\"," +
        "\"fileName\": \"string\"," +
        "\"publicationDate\": \"2023-10-12\"," +
        "}"

    const val REFERENCED_REPORTS_DEFAULT_VALUE: String = "{" +
        "\"string\": $companyReport" +
        "}"
}
