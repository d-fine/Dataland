package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.model.DataIdAndHashCompositeKey

/**
 * The database entity for storing mapping between Dataland data and EuroDaT
 */
@Entity
@Table(
    name = "dataland_eurodat_data_mapping",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["dataId", "hash"]),
    ],
)
@IdClass(DataIdAndHashCompositeKey::class)
data class DataIdAndHashToEurodatIdMappingEntity(
    @Id
    @Column(name = "data_id")
    val dataId: String,
    @Id
    @Column(name = "hash")
    var hash: String,
    @Column(name = "eurodat_id")
    var eurodatId: String,
)
