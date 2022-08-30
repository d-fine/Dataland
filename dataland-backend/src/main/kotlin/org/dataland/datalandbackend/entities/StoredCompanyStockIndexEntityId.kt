package org.dataland.datalandbackend.entities

import org.dataland.datalandbackend.model.enums.company.StockIndex
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

/**
 * The composite ID for the StoredCompanyStockIndex Entity
 */
@Embeddable
data class StoredCompanyStockIndexEntityId(
    @Column(name = "company_id")
    var companyId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_index")
    var stockIndex: StockIndex,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1
    }
}
