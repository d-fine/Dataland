package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Disregard for freedom of association"
 */
data class LksgDisregardForFreedomOfAssociation(
    val freedomOfAssociation: YesNo?,

    val representedEmployees: BigDecimal?,

    val discriminationForTradeUnionMembers: YesNo?,

    val freedomOfOperationForTradeUnion: YesNo?,

    val freedomOfAssociationTraining: YesNo?,

    val worksCouncil: YesNo?,
)
