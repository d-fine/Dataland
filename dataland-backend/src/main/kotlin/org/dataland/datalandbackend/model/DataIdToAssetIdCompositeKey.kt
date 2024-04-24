package org.dataland.datalandbackend.model
import java.io.Serializable

/**
 * The primary key in the dataId-assetId-mapping-table is a composite key. In order to define that composite key, this
 * class here is required.
 */
class DataIdToAssetIdCompositeKey : Serializable { // TODO name ok?
    val dataId: String? = null // TODO why nullable
    val assetId: String? = null // TODO why nullable   TODO Emanuel: Naming?  Ist das nicht nur noch hash oder "JSON"?
    val eurodatId: String? = null // TODO why nullable   TODO Emanuel: Naming??
}
