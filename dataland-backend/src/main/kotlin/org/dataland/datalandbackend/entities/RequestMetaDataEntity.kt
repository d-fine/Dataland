package org.dataland.datalandbackend.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * The entity storing data regarding invitation request meta data
 */
@Entity
@Table(name = "stored_invitation_request_meta_data")
data class RequestMetaDataEntity(
    @Id
    @Column(name = "request_id")
    val requestId: String,

    @Column(name = "user_id")
    var userId: String,

    @Column(name = "user_id")
    var uploadId: String?,

    @Column(name = "timestamp")
    var timeStamp: String,
)