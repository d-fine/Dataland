package org.dataland.datalandbackend.model
import java.io.Serializable
class DataIdToAssetIdCompositeKey : Serializable { // TODO name ok?
    val dataId: String? = null // TODO why nullable
    val assetId: String? = null // TODO why nullable
}
