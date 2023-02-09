package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * The entity storing data regarding invites
 */
@Entity
@Table(name = "invite_meta_info")
data class InviteMetaInfoEntity(
    @Id
    @Column(name = "invite_id")
    val inviteId: String,

    @Column(name = "user_id")
    var userId: String,

    @Column(name = "file_id")
    var fileId: String,

    @Column(name = "timestamp")
    var timeStamp: String,

    @Column(name = "was_invite_successful")
    var wasInviteSuccessful: Boolean,

    @Column(name = "invite_result_message")
    val inviteResultMessage: String,
)
