package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.entities.BasicCompanyInformation

/**
 * API-Model
 * The result of a company identifier validation
 */
data class CompanyIdentifierValidationResult(
    @field:JsonProperty(required = true)
    val identifier: String,
    val companyInformation: BasicCompanyInformation? = null,
) {
    constructor(
        identifier: String,
        companyId: String,
        companyName: String,
        headquarters: String,
        countryCode: String,
        sector: String? = null,
        lei: String? = null,
    ) : this(
        identifier,
        object : BasicCompanyInformation {
            override val companyId = companyId
            override val companyName = companyName
            override val headquarters = headquarters
            override val countryCode = countryCode
            override val sector = sector
            override val lei: String? = lei
        },
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        with(other as CompanyIdentifierValidationResult) {
            return this.identifier == identifier &&
                this.companyInformation?.companyId == companyInformation?.companyId &&
                this.companyInformation?.companyName == companyInformation?.companyName &&
                this.companyInformation?.headquarters == companyInformation?.headquarters &&
                this.companyInformation?.countryCode == companyInformation?.countryCode &&
                this.companyInformation?.sector == companyInformation?.sector &&
                this.companyInformation?.lei == companyInformation?.lei
        }
    }
}
