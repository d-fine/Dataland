package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

/**
 * Data class containing the relevant information from the GLEIF xml files
 */

@JsonIgnoreProperties(ignoreUnknown = true)
data class GleifLEIData(
    @field:JacksonXmlProperty(localName = "LEIRecords")
    val leiRecords: List<LEIRecord>,
)

/**
 * Data class containing the relevant Lei Record information from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LEIRecord(
    @field:JacksonXmlProperty(localName = "LEI")
    val lei: String,
    @field:JacksonXmlProperty(localName = "Entity")
    val entity: Entity,
)

/**
 * Data class containing the relevant Entity information from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Entity(
    @field:JacksonXmlProperty(localName = "LegalName")
    val legalName: LegalName,
    @field:JacksonXmlProperty(localName = "HeadquartersAddress")
    val headquartersAddress: HeadquartersAddress,
    @field:JacksonXmlProperty(localName = "OtherEntityNames")
    val otherEntityNames: List<AlternativeEntityName>? = null,
    @field:JacksonXmlProperty(localName = "TransliteratedOtherEntityNames")
    val transliteratedOtherEntityNames: List<AlternativeEntityName>? = null,
)

/**
 * Data class containing the company name from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LegalName(
    @field:JacksonXmlProperty(isAttribute = true, localName = "lang")
    val lang: String? = null,
    @field:JacksonXmlText
    val name: String = "",
)

/**
 * Data class containing the relevant alternative entity names from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlternativeEntityName(
    @field:JacksonXmlProperty(isAttribute = true, localName = "type")
    val type: String? = null,
    @field:JacksonXmlProperty(isAttribute = true, localName = "lang")
    val lang: String? = null,
    @field:JacksonXmlText
    val name: String = "",
)

/**
 * Data class containing the relevant location information from the GLEIF xml files
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class HeadquartersAddress(
    @field:JacksonXmlProperty(localName = "City")
    val city: String,
    @field:JacksonXmlProperty(localName = "PostalCode")
    val postalCode: String? = null,
    @field:JacksonXmlProperty(localName = "Country")
    val country: String,
)
