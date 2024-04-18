package org.dataland.datalandbackend.model
import java.io.Serializable

/**
 * The primary key in the dataId-assetId-mapping-table is a composite key. In order to define that composite key, this
 * class here is required.
 */

data class DataIdToAssetIdCompositeKey(
    val dataId: String,
    val assetId: String,
    val eurodatId: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
