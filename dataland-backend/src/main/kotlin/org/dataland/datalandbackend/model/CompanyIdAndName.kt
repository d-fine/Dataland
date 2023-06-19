package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*

/**
 * --- API model ---
 * Class containing only a company's id and name
 */
@Entity
@SqlResultSetMapping(
    classes = [ConstructorResult(
        targetClass = CompanyIdAndName::class,
        columns = [
            ColumnResult(name = "companyId", type = String::class),
            ColumnResult(name = "companyName", type = String::class),
        ],
    )]
)
data class CompanyIdAndName(
    @field:JsonProperty(required = true)
    @Id
    val companyId: String,
    @field:JsonProperty(required = true)
    val companyName: String,
)
