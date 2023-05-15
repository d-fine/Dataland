package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Disregard for freedom of association"
 */
data class LksgDisregardForFreedomOfAssociation(
        val freedomOfAssociation: BaseDataPoint<YesNo>?,

        val representedEmployees: BaseDataPoint<BigDecimal>?,

        val discriminationForTradeUnionMembers: BaseDataPoint<YesNo>?,

        val freedomOfOperationForTradeUnion: BaseDataPoint<YesNo>?,

        val freedomOfAssociationTraining: BaseDataPoint<YesNo>?,

        val worksCouncil: BaseDataPoint<YesNo>?,
)
