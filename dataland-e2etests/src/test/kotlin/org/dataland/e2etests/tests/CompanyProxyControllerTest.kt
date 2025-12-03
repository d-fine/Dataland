package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyProxyString
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.GlobalAuth.jwtHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyProxyControllerTest {
    private val apiAccessor = ApiAccessor()
    private val companyProxyApi = apiAccessor.companyProxyController

    @BeforeEach
    fun cleanSetup() {
    }

    private fun uploadCompanyAsUploader(): String =
        GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        }

    @Test
    fun `create proxy then get by id returns correct data`() {
        val companyIdProxyCompany = uploadCompanyAsUploader()
        val companyIdProxiedCompany = uploadCompanyAsUploader()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val proxyId =
            companyProxyApi
                .postCompanyProxy(
                    CompanyProxyString(
                        proxiedCompanyId = companyIdProxiedCompany,
                        proxyCompanyId = companyIdProxyCompany,
                        framework = "sfdr",
                        reportingPeriod = "2024",
                    ),
                ).proxyId

        val retrievedProxy = companyProxyApi.getCompanyProxyById(proxyId)

        assertEquals(retrievedProxy.proxiedCompanyId, companyIdProxiedCompany)
        assertEquals(retrievedProxy.proxyCompanyId, companyIdProxyCompany)
        assertEquals(retrievedProxy.framework, "sfdr")
        assertEquals(retrievedProxy.reportingPeriod, "2024")
    }
}
