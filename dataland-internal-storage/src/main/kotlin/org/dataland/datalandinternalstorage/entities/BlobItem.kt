package org.dataland.datalandinternalstorage.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table

/**
 * The database entity for storing blobs
 */
@Entity
@Table(name = "blob_items")
data class BlobItem(
    @Id
    @Column(name = "blob_id")
    val blobId: String,

    @Lob
    @Column(name = "data")
    val data: ByteArray,
)
