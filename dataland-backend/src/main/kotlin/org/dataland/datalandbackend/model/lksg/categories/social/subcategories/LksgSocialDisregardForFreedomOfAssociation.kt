package org.dataland.datalandbackend.model.lksg.categories.social.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Disregard for freedom of association" belonging to the category "Social" of the
 * Lksg framework.
*/
data class LksgSocialDisregardForFreedomOfAssociation(
    val freedomOfAssociation: YesNo? = null,

    val employeeRepresentationInPercent: BigDecimal? = null,

    val discriminationForTradeUnionMembers: YesNo? = null,

    val freedomOfOperationForTradeUnion: YesNo? = null,

    val freedomOfAssociationTraining: YesNo? = null,

    val worksCouncil: YesNo? = null,
)
