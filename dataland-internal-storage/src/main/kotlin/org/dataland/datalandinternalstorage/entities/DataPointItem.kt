package org.dataland.datalandinternalstorage.entities

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandbackendutils.interfaces.DataPointInstance
import org.dataland.datalandinternalstorage.model.StorableDataPoint
import java.util.UUID

/**
 * The database entity for storing data points
 */
@Entity
@Table(name = "data_point_items")
data class DataPointItem(
    @Id
    @Column(name = "data_id")
    val dataId: UUID,
    @Column(name = "company_id")
    override val companyId: UUID,
    @Column(name = "reporting_period")
    override val reportingPeriod: String,
    @Column(name = "data_point_identifier")
    override val dataPointIdentifier: String,
    @Column(name = "data", columnDefinition = "TEXT")
    override val dataPointContent: String,
) : DataPointInstance {
    /**
     * Converts the DataPointItem to a StorableDataPoint
     * @return a StorableDataPoint object
     */
    fun toStorableDataPoint(objectMapper: ObjectMapper): StorableDataPoint =
        StorableDataPoint(
            dataPointContent = objectMapper.readValue(dataPointContent, String::class.java),
            dataPointIdentifier = dataPointIdentifier,
            companyId = companyId,
            reportingPeriod = reportingPeriod,
        )
}
