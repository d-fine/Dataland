package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.dataland.datalandbackend.model.DataIdToHashCompositeKey

/**
 * The database entity for storing mapping between data and documents
 */
@Entity
@Table(
    name = "data_document_mapping",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["dataId", "hash", "eurodatId"]),
    ],
)
@IdClass(DataIdToHashCompositeKey::class)
data class DataIdToHashMappingEntity(
    @Id
    @Column(name = "data_id")
    val dataId: String,
    @Id
    @Column(name = "hash")
    var hash: String,
    @Id
    @Column(name = "eurodat_id")
    var eurodatId: String,

)
