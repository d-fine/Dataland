package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDetailsPerCashFlowType
import java.math.BigDecimal

class DummyDataCreator {

    internal fun createEuTaxonomyTestDataSet(): EuTaxonomyData {
        return EuTaxonomyData(
            EuTaxonomyData.ReportingObligation.yes, EuTaxonomyData.Attestation.fullAssurance,
            capex = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(52705000),
                aligned = BigDecimal(20),
                eligible = BigDecimal(10)
            ),
            opex = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(80490000),
                aligned = BigDecimal(15),
                eligible = BigDecimal(5)
            ),
            revenue = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(432590000),
                aligned = BigDecimal(5),
                eligible = BigDecimal(3)
            )
        )
    }
}
