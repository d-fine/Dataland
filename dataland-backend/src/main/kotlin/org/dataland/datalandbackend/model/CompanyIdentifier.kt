package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.hibernate.annotations.Immutable
import javax.persistence.*

/**
 * --- API model ---
 * Class for defining the company identifiers as a part of company information
 * @param identifierType type of the identifier
 * @param identifierValue value of the identifier
 */
@Entity
@Immutable
@Table(name = "company_identifiers")
@IdClass(CompanyIdentifierId::class)
data class CompanyIdentifier(
    @Id
    @Column(name="identifier_value")
    @field:JsonProperty("identifierValue", required = true)
    val identifierValue: String,

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name="identifier_type")
    @field:JsonProperty("identifierType", required = true)
    val identifierType: IdentifierType,

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company : StoredCompany? = null
)
