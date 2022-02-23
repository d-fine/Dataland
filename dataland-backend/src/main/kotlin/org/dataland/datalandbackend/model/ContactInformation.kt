package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class ContactInformation(
    @field:JsonProperty("Name", required = true) val name: String,
    @field:JsonProperty("Address", required = true) val address: List<String>,
    @field:JsonProperty("Website", required = true) val website: List<String>,
    @field:JsonProperty("Email", required = true) val email: List<String>,
    @field:JsonProperty("Phone", required = true) val phone: List<String>
)
