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

    const val PRODUCT_CATEGORIES_DEFAULT_VALUE: String = "{\n" +
        "          \"Products\": {\n" +
        "            \"definitionProductTypeService\": [\n" +
        "              \"string\"\n" +
        "            ],\n" +
        "            \"suppliersPerCountry\": [\n" +
        "              {\n" +
        "                \"country\": \"string\",\n" +
        "                \"numberOfSuppliers\": 0\n" +
        "              }\n" +
        "            ],\n" +
        "            \"orderVolume\": 0\n" +
        "          },\n" +
        "          \"Services\": {\n" +
        "            \"definitionProductTypeService\": [\n" +
        "              \"string\"\n" +
        "            ],\n" +
        "            \"suppliersPerCountry\": [\n" +
        "              {\n" +
        "                \"country\": \"string\",\n" +
        "                \"numberOfSuppliers\": 0\n" +
        "              }\n" +
        "            ],\n" +
        "            \"orderVolume\": 0\n" +
        "          },\n" +
        "          \"RawMaterials\": {\n" +
        "            \"definitionProductTypeService\": [\n" +
        "              \"string\"\n" +
        "            ],\n" +
        "            \"suppliersPerCountry\": [\n" +
        "              {\n" +
        "                \"country\": \"string\",\n" +
        "                \"numberOfSuppliers\": 0\n" +
        "              }\n" +
        "            ],\n" +
        "            \"orderVolume\": 0\n" +
        "          }\n" +
        "        }"
}
