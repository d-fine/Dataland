package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonValue
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.hibernate.annotations.Immutable
import javax.persistence.*

@Entity
@Immutable
@Table(name = "stored_company_stock_indices")
data class StoredCompanyStockIndexEntity(

    @EmbeddedId
    val id: StoredCompanyStockIndexEntityId,

    @MapsId("companyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    var company: StoredCompanyEntity
) {
    @JsonValue
    fun toApiModel(): StockIndex {
        return id.stockIndex
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}