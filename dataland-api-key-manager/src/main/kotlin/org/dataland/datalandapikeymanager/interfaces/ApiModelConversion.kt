package org.dataland.datalandapikeymanager.interfaces

import com.fasterxml.jackson.annotation.JsonValue

/**
 * A class implementing this interface should be a database entity
 * that offers a conversion into a specified API Model
 */
interface ApiModelConversion<T> {
    /**
     * Returns the API-Model for specified database entity
     */
    @JsonValue
    fun toApiModel(): T
}


// TODO do we really need this since there is only one toApiModel conversion needed