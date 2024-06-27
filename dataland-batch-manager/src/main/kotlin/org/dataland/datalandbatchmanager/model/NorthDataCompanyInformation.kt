package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Data class containing the relevant information from the Northdata csv files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NorthDataCompanyInformation(
    @JsonProperty("Entity.LegalName")
    val companyName: String,

    @JsonProperty("Entity.Ort")
    val headquarters: String,

    @JsonProperty("Entity.PLZ")
    val headquartersPostalCode: String,

    @JsonProperty("LEI")
    val lei: String,

    @JsonProperty("Entity.Land")
    val countryCode: String,

    @JsonProperty("Entity.RegisterId")
    val registerId: String,

    @JsonProperty("Entity.Straße")
    val street: String,

    @JsonProperty("Entity.USt.-Id")
    val vatId: String,

    @JsonProperty("Entity.Status")
    val status: String,
)
