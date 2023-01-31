package org.dataland.datalandbackend.interfaces

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.keycloakAdapter.auth.DatalandLegacyAuthentication

/**
 * A class implementing this interface should be a database entity
 * that offers a conversion into a specified API Model
 */
interface ApiModelConversion<T> {
    /**
     * Returns the API-Model for specified database entity
     */
    @JsonValue
    fun toApiModel(viewingUser: DatalandLegacyAuthentication?): T
}
