package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DataSet(
    @field:JsonProperty("name", required = true) val name: String,
    @field:JsonProperty("payload", required = true) val payload: String

    fun doNothing() {
        println("Doing nothing with name $name")
    }
)
