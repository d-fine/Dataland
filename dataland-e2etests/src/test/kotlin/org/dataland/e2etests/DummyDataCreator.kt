package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDetailsPerCashFlowType
import java.math.BigDecimal
import java.time.LocalDate

class DummyDataCreator {

    internal fun createEuTaxonomyTestData(revenue: Int): EuTaxonomyData {
        return EuTaxonomyData(
            EuTaxonomyData.ReportingObligation.yes, EuTaxonomyData.Attestation.reasonableAssurance,
            capex = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(revenue * 0.1),
                aligned = BigDecimal(revenue * 0.1 * 0.2),
                eligible = BigDecimal(revenue * 0.1 * 0.15)
            ),
            opex = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(revenue * 0.4),
                aligned = BigDecimal(revenue * 0.4 * 0.15),
                eligible = BigDecimal(revenue * 0.4 * 0.05)
            ),
            revenue = EuTaxonomyDetailsPerCashFlowType(
                total = BigDecimal(revenue),
                aligned = BigDecimal(revenue * 0.05),
                eligible = BigDecimal(revenue * 0.03)
            )
        )
    }

    internal fun createCompanyTestInformation(testDataMarker: String): CompanyInformation {
        return CompanyInformation(
            companyName = "Test-Company_$testDataMarker",
            headquarters = "Test-Headquarters_$testDataMarker",
            sector = "Test-Sector_$testDataMarker",
            marketCap = BigDecimal(125670200),
            reportingDateOfMarketCap = LocalDate.of(2022, 1, 1)
        )
    }
}
