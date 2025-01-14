package org.dataland.datalandinternalstorage.entities

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.interfaces.DataPointInstance
import org.dataland.datalandinternalstorage.model.StorableDataPoint

/**
 * The database entity for storing data points
 */
@Entity
@Table(name = "data_point_items")
data class DataPointItem(
    @Id
    @Column(name = "data_point_id")
    val dataPointId: String,
    @Column(name = "company_id")
    override val companyId: String,
    @Column(name = "reporting_period")
    override val reportingPeriod: String,
    @Column(name = "data_point_type")
    override val dataPointType: String,
    @Column(name = "data", columnDefinition = "TEXT")
    override val dataPoint: String,
) : DataPointInstance {
    /**
     * Converts the DataPointItem to a StorableDataPoint
     * @return a StorableDataPoint object
     */
    fun toStorableDataPoint(objectMapper: ObjectMapper): StorableDataPoint =
        StorableDataPoint(
            dataPoint = objectMapper.readValue(dataPoint, String::class.java),
            dataPointType = dataPointType,
            companyId = companyId,
            reportingPeriod = reportingPeriod,
        )
}
