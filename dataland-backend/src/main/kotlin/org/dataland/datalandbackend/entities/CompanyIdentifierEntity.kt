package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.data.domain.Persistable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * The database entity for storing company identifiers
 */
@Entity
@Table(name = "company_identifiers")
@IdClass(CompanyIdentifierId::class)
data class CompanyIdentifierEntity(
    @Id
    @Column(name = "identifier_value")
    val identifierValue: String,

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type")
    val identifierType: IdentifierType,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity?,

    @Transient
    private var isNew: Boolean = false
) : Persistable<CompanyIdentifierId>, ApiModelConversion<CompanyIdentifier> {
    override fun getId(): CompanyIdentifierId = CompanyIdentifierId(identifierValue, identifierType)
    override fun isNew(): Boolean = isNew

    override fun toApiModel(): CompanyIdentifier {
        return CompanyIdentifier(
            identifierValue = identifierValue,
            identifierType = identifierType,
        )
    }
}
