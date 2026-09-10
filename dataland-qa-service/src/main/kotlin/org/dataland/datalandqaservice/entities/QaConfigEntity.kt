package org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.converters.PreApprovalConfigConverter
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.hibernate.envers.Audited
import java.util.UUID

/**
 * JPA entity storing the singleton pre-approval configuration row.
 *
 * The table this entity is mapped to must always contain exactly one row, identified by
 * [QA_CONFIG_SINGLETON_ID]. This guarantee is owned by the Flyway migration that creates and seeds the table.
 */
@Entity
@Audited
@Table(name = "qa_config")
class QaConfigEntity(
    @Id
    val id: UUID = QA_CONFIG_SINGLETON_ID,
    @Column(columnDefinition = "TEXT", nullable = false)
    @Convert(converter = PreApprovalConfigConverter::class)
    var config: PreApprovalConfig,
) {
    companion object {
        /**
         * The hardcoded identifier of the single row that this entity's table may ever contain.
         */
        val QA_CONFIG_SINGLETON_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }
}
