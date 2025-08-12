package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/**
 * Data class containing the relevant information from the GLEIF xml files
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class GleifLEIData(
    @JacksonXmlProperty(localName = "LEIRecords")
    val leiRecords: List<LEIRecord>,
)

/**
 * Data class containing the relevant Lei Record information from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LEIRecord(
    @JacksonXmlProperty(localName = "LEI")
    val lei: String,
    @JacksonXmlProperty(localName = "Entity")
    val entity: Entity,
)

/**
 * Data class containing the relevant Entity information from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Entity(
    @JacksonXmlProperty(localName = "LegalName")
    val legalName: String,
    @JacksonXmlProperty(localName = "HeadquartersAddress")
    val headquartersAddress: HeadquartersAddress,
)

/**
 * Data class containing the relevant location information from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class HeadquartersAddress(
    @JacksonXmlProperty(localName = "City")
    val city: String,
    @JacksonXmlProperty(localName = "PostalCode")
    val postalCode: String,
    @JacksonXmlProperty(localName = "Country")
    val country: String,
   /* @JacksonXmlProperty(localName = "lang", isAttribute = true)
    val lang: String? = null,*/
)
