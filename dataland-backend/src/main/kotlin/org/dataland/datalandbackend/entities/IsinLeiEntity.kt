package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
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
)
data class IsinLeiEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity?,
    @Id
    @Column(name = "isin", nullable = false, unique = true)
    val isin: String,
    @Column(name = "lei", nullable = true, unique = false)
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
