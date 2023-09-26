package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the P2P questionnaire regarding company policies and governance for all sectors.
 */
data class P2pGovernance(
    val organisationalResponsibilityForParisCompatibility: YesNo? = null,

    val parisCompatibilityInExecutiveRemunerationInPercent: BigDecimal? = null,

    val parisCompatibilityInAverageRemunerationInPercent: BigDecimal? = null,

    val shareOfEmployeesTrainedOnParisCompatibilityInPercent: BigDecimal? = null,

    val qualificationRequirementsOnParisCompatibility: YesNo? = null,

    val mobilityAndTravelPolicy: YesNo? = null,

    val upstreamSupplierEngagementStrategy: YesNo? = null,

    val upstreamSupplierProcurementPolicy: YesNo? = null,

    val downstreamCustomerEngagement: YesNo? = null,

    val policymakerEngagement: YesNo? = null,
)
