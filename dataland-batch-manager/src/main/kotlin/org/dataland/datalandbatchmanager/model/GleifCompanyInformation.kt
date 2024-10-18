package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class containing the relevant information from the GLEIF csv files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class GleifCompanyInformation(
    @JsonProperty("Entity.LegalName")
    val companyName: String,
    @JsonProperty("Entity.HeadquartersAddress.City")
    val headquarters: String,
    @JsonProperty("Entity.HeadquartersAddress.PostalCode")
    val headquartersPostalCode: String,
    @JsonProperty("LEI")
    val lei: String,
    @JsonProperty("Entity.HeadquartersAddress.Country")
    val countryCode: String,
)
