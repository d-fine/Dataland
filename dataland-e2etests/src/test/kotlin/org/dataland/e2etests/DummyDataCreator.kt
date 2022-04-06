package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDetailsPerCashFlowType
import java.math.BigDecimal
import java.time.LocalDate

class DummyDataCreator {

    internal fun createEuTaxonomyTestData(): EuTaxonomyData {
        return EuTaxonomyData(
            EuTaxonomyData.ReportingObligation.yes, EuTaxonomyData.Attestation.reasonableAssurance,
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

    internal fun createCompanyTestInformation(testDataMarker: String): CompanyInformation {
        return CompanyInformation(
            companyName = "Test-Company_$testDataMarker",
            headquarters = "Test-Headquarters_$testDataMarker",
            sector = "Test-Sector_$testDataMarker",
            marketCap = BigDecimal(100),
            reportingDateOfMarketCap = LocalDate.of(2022, 1, 1)
        )
    }
}
