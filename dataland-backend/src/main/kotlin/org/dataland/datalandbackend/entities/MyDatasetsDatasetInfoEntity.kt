package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication

/**
 * The entity storing info for a dataset on the my datasets page
 */
interface MyDatasetsDatasetInfoEntity {
    val companyId: String
    val dataId: String
    val companyName: String
    val dataType: String
    val reportingPeriod: String
    val qualityStatus: Int
    val currentlyActive: Boolean
    var uploadTime: Long
}
