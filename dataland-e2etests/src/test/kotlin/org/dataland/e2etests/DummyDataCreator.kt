package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataSet
import java.math.BigDecimal

class DummyDataCreator {

    internal fun createEuTaxonomyTestDataSet(): EuTaxonomyDataSet {
        return EuTaxonomyDataSet(
            EuTaxonomyDataSet.ReportingObligation.yes, EuTaxonomyDataSet.Attestation.full,
            capex = EuTaxonomyData(
                total = BigDecimal(52705000),
                alignedTurnover = BigDecimal(20),
                eligibleTurnover = BigDecimal(10)
            ),
            opex = EuTaxonomyData(
                total = BigDecimal(80490000),
                alignedTurnover = BigDecimal(15),
                eligibleTurnover = BigDecimal(5)
            ),
            revenue = EuTaxonomyData(
                total = BigDecimal(432590000),
                alignedTurnover = BigDecimal(5),
                eligibleTurnover = BigDecimal(3)
            )
        )
    }
}
