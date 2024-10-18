package org.dataland.datalandbackend.model.p2p.categories.general.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Governance" belonging to the category "General" of the p2p framework.
*/
data class P2pGeneralGovernance(
    val organisationalResponsibilityForParisCompatibility: YesNo? = null,
    val parisCompatibilityInExecutiveRemunerationInPercent: BigDecimal? = null,
    val parisCompatibilityInAverageRemunerationInPercent: BigDecimal? = null,
    val shareOfEmployeesTrainedOnParisCompatibilityInPercent: BigDecimal? = null,
    val qualificationRequirementsOnParisCompatibility: YesNo? = null,
    val mobilityAndTravelPolicy: YesNo? = null,
    val upstreamSupplierEngagementStrategy: YesNo? = null,
    @field:Valid
    val upstreamSupplierProcurementPolicy: BaseDataPoint<YesNo>? = null,
    val downstreamCustomerEngagement: YesNo? = null,
    val policymakerEngagement: YesNo? = null,
)
