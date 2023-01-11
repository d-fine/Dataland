package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Freedom of association"
 */
data class FreedomOfAssociation(
    val freedomOfAssociation: YesNo?,

    val discriminationForTradeUnionMembers: YesNo?,

    val freedomOfOperationForTradeUnion: YesNo?,

    val freedomOfAssociationTraining: YesNo?,

    val worksCouncil: YesNo?,
)