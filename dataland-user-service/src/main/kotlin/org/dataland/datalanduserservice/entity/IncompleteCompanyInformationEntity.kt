package org.dataland.datalanduserservice.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

/**
 * The database entity for the human log of companies whose fiscal year end or sector could not be completed by the AI
 */
@Entity
@Table(name = "incomplete_company_information")
data class IncompleteCompanyInformationEntity(
    @Id
    val companyId: String,
    val creationTimestamp: Long = Instant.now().toEpochMilli(),
)
