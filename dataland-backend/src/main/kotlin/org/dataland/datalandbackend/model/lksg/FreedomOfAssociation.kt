package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo

data class FreedomOfAssociation(
    val freedomOfAssociation: YesNo?,

    val discriminationForTradeUnionMembers: YesNo?,

    val freedomOfOperationForTradeUnion: YesNo?,

    val freedomOfAssociationTraining: YesNo?,

    val worksCouncil: YesNo?,
)