package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.enums.company.StockIndex
import org.hibernate.annotations.Immutable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Table

/**
 * The entity storing which stock indices a company is listed in
 */
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
) : ApiModelConversion<StockIndex> {
    override fun toApiModel(): StockIndex {
        return id.stockIndex
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
