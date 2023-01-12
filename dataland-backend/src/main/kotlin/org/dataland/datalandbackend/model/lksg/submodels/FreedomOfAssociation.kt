package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Freedom of association"
 */
data class FreedomOfAssociation(
    val freedomOfAssociation: YesNo? = null,

    val discriminationForTradeUnionMembers: YesNo? = null,

    val freedomOfOperationForTradeUnion: YesNo? = null,

    val freedomOfAssociationTraining: YesNo? = null,

    val worksCouncil: YesNo? = null,
)