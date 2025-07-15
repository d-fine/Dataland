package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.entities.IsinLeiEntity

/**
 * --- API model ---
 * Class for defining the request body of a put ISIN-LEI mapping request
 * @param isin the ISIN of the company
 * @param lei the LEI of the company
 */
data class IsinLeiMappingData(
    @field:JsonProperty(required = true)
    val isin: String,
    @field:JsonProperty(required = true)
    val lei: String,
) {
    /**
     * Converts this API model to a database entity.
     * @return the corresponding [IsinLeiEntity]
     */
    fun toIsinLeiEntity(): IsinLeiEntity =
        IsinLeiEntity(
            isin = isin,
            lei = lei,
        )
}
