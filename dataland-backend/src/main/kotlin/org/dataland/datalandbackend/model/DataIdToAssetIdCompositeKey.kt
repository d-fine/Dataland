package org.dataland.datalandbackend.model
import jakarta.persistence.Embeddable
import java.io.Serializable

/**
 * The primary key in the dataId-assetId-mapping-table is a composite key. In order to define that composite key, this
 * class here is required.
 */
@Embeddable
data class DataIdToAssetIdCompositeKey(
    var dataId: String,
    var assetId: String,
    var eurodatId: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
