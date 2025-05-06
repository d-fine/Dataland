package org.dataland.datalandbackend.utils

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExampleFormattingConstants {
    private const val EXTENDED_DOCUMENT_REFERENCE =
        "{" +
            "\"page\": 0," +
            "\"tagName\": \"string\"," +
            "\"fileName\": \"string\"," +
            "\"fileReference\": \"string\"" +
            "}"

    private const val TAXONOMY_ELIGIBILITY_ACTIVITY =
        "{" +
            "\"taxonomyEligibleActivityInPercent\": {" +
            "\"value\": 0," +
            "\"quality\": \"Audited\"," +
            "\"dataSource\": $EXTENDED_DOCUMENT_REFERENCE," +
            "\"comment\": \"string\"" +
            "}" +
            "}"

    const val ELIGIBILITY_KPIS_DEFAULT_VALUE: String =
        "{" +
            "\"CreditInstitution\": " +
            TAXONOMY_ELIGIBILITY_ACTIVITY +
            "," +
            "\"InsuranceOrReinsurance\": " +
            TAXONOMY_ELIGIBILITY_ACTIVITY +
            "," +
            "\"AssetManagement\": " +
            TAXONOMY_ELIGIBILITY_ACTIVITY +
            "," +
            "\"InvestmentFirm\": " +
            TAXONOMY_ELIGIBILITY_ACTIVITY +
            "}"

    const val SUBCONTRACTING_COMPANIES_DEFAULT_VALUE =
        "{" +
            "\"DE\": [\"NaceCodeA, NaceCodeB\"]," +
            "\"GB\": [\"NaceCodeC\"]" +
            "}"

    private const val PROCUREMENT_CATEGORY =
        "{" +
            "\"procuredProductTypesAndServicesNaceCodes\": [\"string\"]," +
            "\"numberOfSuppliersPerCountryCode\": {" +
            "\"DE\": null," +
            "\"GB\": 2" +
            "}," +
            "\"shareOfTotalProcurementInPercent\": 0" +
            "}"

    const val PROCUREMENT_CATEGORIES_DEFAULT_VALUE: String =
        "{" +
            "\"Products\": $PROCUREMENT_CATEGORY," +
            "\"Services\": $PROCUREMENT_CATEGORY," +
            "\"RawMaterials\": $PROCUREMENT_CATEGORY" +
            "}"

    private const val DRIVE_MIX =
        "{" +
            "\"driveMixPerFleetSegmentInPercent\": 0," +
            "\"totalAmountOfVehicles\": 0" +
            "}"

    const val DRIVE_MIX_DEFAULT_VALUE: String =
        "{" +
            "\"SmallTrucks\": $DRIVE_MIX," +
            "\"MediumTrucks\": $DRIVE_MIX," +
            "\"LargeTrucks\": $DRIVE_MIX" +
            "}"

    private const val EXTENDED_DATA_POINT_BIG_DECIMAL =
        "{" +
            "\"quality\": \"Audited\"," +
            "\"dataSource\": $EXTENDED_DOCUMENT_REFERENCE," +
            "\"comment\": \"string\"," +
            "\"value\": 0" +
            "}"

    private const val HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION =
        "{" +
            "\"highImpactClimateSectorEnergyConsumptionInGWh\": $EXTENDED_DATA_POINT_BIG_DECIMAL," +
            "\"highImpactClimateSectorEnergyConsumptionInGWhPerMillionEURRevenue\": $EXTENDED_DATA_POINT_BIG_DECIMAL" +
            "}"

    const val HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE: String =
        "{" +
            "\"NaceCodeA\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeB\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeC\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeD\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeE\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeF\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeG\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeH\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION," +
            "\"NaceCodeL\": $HIGH_IMPACT_CLIMATE_SECTOR_ENERGY_CONSUMPTION" +
            "}"

    private const val COMPANY_REPORT =
        "{" +
            "\"fileReference\": \"string\"," +
            "\"fileName\": \"string\"," +
            "\"publicationDate\": \"2023-10-12\"" +
            "}"

    const val REFERENCED_REPORTS_DEFAULT_VALUE: String =
        "{" +
            "\"string\": $COMPANY_REPORT" +
            "}"
}
