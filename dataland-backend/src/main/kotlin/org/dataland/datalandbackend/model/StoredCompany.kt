package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
// import javax.persistence.ElementCollection

/**
 * --- API model ---
 * Class for defining the meta data of a company
 * @param companyId identifies the company
 * @param companyInformation contains information of company
 * @param dataRegisteredByDataland contains meta info for all data sets of this company
 */
@Entity
@Table(name = "stored_companies")
data class StoredCompany(
    @Id
    @Column(name = "company_id")
    @field:JsonProperty(required = true)
    val companyId: String,

    @Embedded
    @field:JsonProperty(required = true)
    val companyInformation: CompanyInformation,

    @OneToMany(mappedBy = "company")
    @field:JsonProperty(required = true)
    val dataRegisteredByDataland: MutableList<DataMetaInformation>
)
