package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding company policies and governance for all sectors.
 */
data class P2pGovernance(
    val organisationalResponsibilityForParisCompatibility: YesNo?,

    val parisCompatibilityInExecutiveRemuneration: BigDecimal?,

    val parisCompatibilityInAverageRemuneration: BigDecimal?,

    val shareOfEmployeesTrainedOnParisCompatibility: BigDecimal?,

    val qualificationRequirementsOnParisCompatibility: YesNo?,

    val mobilityAndTravelPolicy: YesNo?,

    val downstreamCustomerEngagement: YesNo?,

    val policymakerEngagement: YesNo?,

    val upstreamSupplierEngagementStrategy: YesNo?,

    val upstreamSupplierProcurementPolicy: YesNo?,
)
