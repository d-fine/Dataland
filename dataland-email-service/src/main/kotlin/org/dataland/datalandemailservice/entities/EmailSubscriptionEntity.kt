package org.dataland.datalandemailservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * Represents an email subscription entity.
 *
 * @property uuid Unique identifier for the email subscription.
 * @property emailAddress The email address of the subscriber. Must be unique.
 * @property isSubscribed Indicates whether the email address is currently subscribed.
 */
@Entity
@Table(name = "email_subscriptions")
data class EmailSubscriptionEntity(
    /**
     * The unique UUID for the email subscription.
     */
    @Id
    @Column(name = "uuid", nullable = false, updatable = false)
    val uuid: UUID = UUID.randomUUID(),
    /**
     * The subscriber's email address.
     * Must be unique across all subscription entities.
     */
    @Column(name = "email_address", nullable = false, unique = true)
    val emailAddress: String,
    /**
     * Subscription status of the email address.
     * `true` if subscribed, `false` otherwise.
     */
    @Column(name = "is_subscribed", nullable = false)
    var isSubscribed: Boolean,
)
