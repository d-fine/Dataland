package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackend.model.enums.company.IdentifierType

/**
 * The database entity for mapping ISINs to LEIs.
 */
@Entity
@Table(
    name = "isin_lei_mapping",
    indexes = [
        Index(name = "idx_company_id", columnList = "company_id"),
        Index(name = "idx_lei", columnList = "lei"),
    ],
)
data class IsinLeiEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity,
    @Id
    @Column(name = "isin", nullable = false, unique = true)
    val isin: String,
    @Column(name = "lei")
    val lei: String?,
) {
    /**
     * Function to cast this [IsinLeiEntity] into an [CompanyIdentifierEntity]
     */
    fun toCompanyIdentifierEntity(): CompanyIdentifierEntity =
        CompanyIdentifierEntity(
            identifierValue = isin,
            identifierType = IdentifierType.Isin,
            company = company,
        )
}
