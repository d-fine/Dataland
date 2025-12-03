package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
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

    private fun postProxyRelationForRandomCompanies(role: TechnicalUser): String {
        val companyIdProxiedCompany = uploadCompanyAsUploader()
        val companyIdProxyCompany = uploadCompanyAsUploader()
        GlobalAuth.withTechnicalUser(role) {
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

            return proxyId
        }
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
    fun `creating a proxy with invalid inputs returns an error`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        val ex =
            assertThrows<ClientException> {
                companyProxyApi.postCompanyProxy(
                    CompanyProxyString(
                        proxiedCompanyId = "123",
                        proxyCompanyId = "456",
                        framework = "lksg",
                        reportingPeriod = "2023",
                    ),
                )
            }

        assert(ex.message?.contains("400") == true)
    }

    @Test
    fun `create proxy, then delete proxy and assert that it is no longer retrievable`() {
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

        assertEquals(deletedCompanyProxy.proxyId, proxyId)
        val ex = assertThrows<ClientException> { companyProxyApi.getCompanyProxyById(proxyId) }
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

        companyProxyApi.putCompanyProxy(
            proxyId,
            CompanyProxyString(
                proxiedCompanyId = companyIdProxiedCompany,
                proxyCompanyId = companyIdProxyCompany,
                framework = "lksg",
                reportingPeriod = "2023",
            ),
        )
        val retrievedProxy = companyProxyApi.getCompanyProxyById(proxyId)

        assertEquals(retrievedProxy.proxiedCompanyId, companyIdProxiedCompany)
        assertEquals(retrievedProxy.proxyCompanyId, companyIdProxyCompany)
        assertEquals(retrievedProxy.framework, "lksg")
        assertEquals(retrievedProxy.reportingPeriod, "2023")
    }

    @Test
    fun `trying to create a proxy as a non-admin user results in a 403`() {
        val ex =
            assertThrows<ClientException> {
                postProxyRelationForRandomCompanies(TechnicalUser.Uploader)
            }

        assertTrue(ex.message?.contains("403") == true)
    }

    @Test
    fun `trying to delete a proxy as a non-admin user results in a 401`() {
        val proxyId = postProxyRelationForRandomCompanies(TechnicalUser.Admin)

        assertThrows<ClientException> {
            GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
                companyProxyApi.deleteCompanyProxy(proxyId)
            }
        }
    }

    @Test
    fun `trying to get a proxy relation by proxyId as a non-admin user results in a 401`() {
        val proxyId = postProxyRelationForRandomCompanies(TechnicalUser.Admin)

        assertThrows<ClientException> {
            GlobalAuth.withTechnicalUser(TechnicalUser.Uploader) {
                companyProxyApi.getCompanyProxyById(proxyId)
            }
        }
    }
}
