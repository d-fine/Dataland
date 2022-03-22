package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDetailsPerCashFlowType
import java.math.BigDecimal

class DummyDataCreator {

    internal fun createEuTaxonomyTestDataSet(): EuTaxonomyData {
        return EuTaxonomyData(
            EuTaxonomyData.ReportingObligation.yes, EuTaxonomyData.Attestation.full,
            capex = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(52705000),
                alignedTurnover = BigDecimal(20),
                eligibleTurnover = BigDecimal(10)
            ),
            opex = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(80490000),
                alignedTurnover = BigDecimal(15),
                eligibleTurnover = BigDecimal(5)
            ),
            revenue = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(432590000),
                alignedTurnover = BigDecimal(5),
                eligibleTurnover = BigDecimal(3)
            )
        )
    }
}
