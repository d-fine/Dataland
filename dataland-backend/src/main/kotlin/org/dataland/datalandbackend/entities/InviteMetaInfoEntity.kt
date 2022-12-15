package org.dataland.datalandbackend.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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

    @Column(name = "is_invite_successful")
    var isInviteSuccessful: Boolean,

    @Column(name = "invite_result_message")
    var inviteResultMessage: String
)
