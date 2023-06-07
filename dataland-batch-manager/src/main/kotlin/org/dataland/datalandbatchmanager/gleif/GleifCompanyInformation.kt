package org.dataland.datalandbatchmanager.gleif

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

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
