package org.dataland.datalandbackend.entities

/**
 * A variation of DataMetaInformationForMyDatasets for database interaction
 */
interface DatasetMetaInfoEntityForMyDatasets {
    val companyId: String
    val dataId: String
    val companyName: String
    val dataType: String
    val reportingPeriod: String
    val qualityStatus: Int
    val currentlyActive: Boolean?
    var uploadTime: Long
}
