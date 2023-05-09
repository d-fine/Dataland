package org.dataland.datalandbackend.interfaces

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * A class implementing this interface should be a database entity
 * that offers a conversion into a specified API Model
 */
fun interface ApiModelConversion<T> {
    /**
     * Returns the API-Model for specified database entity
     */
    @JsonValue
    fun toApiModel(viewingUser: DatalandAuthentication?): T
}
