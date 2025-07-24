package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.data.domain.Persistable

/**
 * The database entity for storing company identifiers
 */
@Entity
@Table(name = "company_identifiers")
@IdClass(CompanyIdentifierEntityId::class)
data class CompanyIdentifierEntity(
    @Id
    @Column(name = "identifier_value")
    val identifierValue: String,
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type")
    val identifierType: IdentifierType,
    // Field has nullable type only for lazy loading - it cannot actually be null in the database due to `optional = false`.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    @JsonBackReference
    var company: StoredCompanyEntity?,
    @Transient
    private var isNew: Boolean = false,
) : Persistable<CompanyIdentifierEntityId> {
    override fun getId(): CompanyIdentifierEntityId = CompanyIdentifierEntityId(identifierValue, identifierType)

    override fun isNew(): Boolean = isNew
}
