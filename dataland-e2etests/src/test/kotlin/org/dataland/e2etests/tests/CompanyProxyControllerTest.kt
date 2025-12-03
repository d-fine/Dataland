package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyProxyString
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.GlobalAuth.jwtHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun `create proxy then delete proxy`() {
        val companyIdProxyCompany = uploadCompanyAsUploader()
        val companyIdProxiedCompany = uploadCompanyAsUploader()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val proxyId =
            companyProxyApi
                .postCompanyProxy(
                    companyProxyString =
                        CompanyProxyString(
                            proxiedCompanyId = companyIdProxiedCompany,
                            proxyCompanyId = companyIdProxyCompany,
                            framework = "sfdr",
                            reportingPeriod = "2024",
                        ),
                ).proxyId

        val deletedCompanyProxy = companyProxyApi.deleteCompanyProxy(proxyId)

        // (4-1) assert that the correct proxy was deleted
        assertEquals(deletedCompanyProxy.proxyId, proxyId)
        assertEquals(deletedCompanyProxy.proxiedCompanyId, companyIdProxiedCompany)
        assertEquals(deletedCompanyProxy.proxyCompanyId, companyIdProxyCompany)
        assertEquals(deletedCompanyProxy.framework, "sfdr")
        assertEquals(deletedCompanyProxy.reportingPeriod, "2024")

        // (4-2) assert that the proxy is no longer retrievable
        val ex = assertThrows<Exception> { companyProxyApi.getCompanyProxyById(proxyId) }
        assertTrue(ex.message?.contains("404") == true)
    }

    @Test
    fun `change existing company proxy using put request`() {
        // 1: create a proxy
        val companyIdProxyCompany = uploadCompanyAsUploader()
        val companyIdProxiedCompany = uploadCompanyAsUploader()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val proxyId =
            companyProxyApi
                .postCompanyProxy(
                    companyProxyString =
                        CompanyProxyString(
                            proxiedCompanyId = companyIdProxiedCompany,
                            proxyCompanyId = companyIdProxyCompany,
                            framework = "sfdr",
                            reportingPeriod = "2024",
                        ),
                ).proxyId

        // 2: change the proxy using put endpoint
        companyProxyApi.putCompanyProxy(
            proxyId,
            CompanyProxyString(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "Lksg",
                reportingPeriod = "2023",
            ),
        )
        // 3: assert that the change was successful
        val retrievedProxy = companyProxyApi.getCompanyProxyById(proxyId)

        assertEquals(retrievedProxy.proxiedCompanyId, companyIdProxiedCompany)
        assertEquals(retrievedProxy.proxyCompanyId, companyIdProxyCompany)
        assertEquals(retrievedProxy.framework, "Lksg")
        assertEquals(retrievedProxy.reportingPeriod, "2023")
    }
}
