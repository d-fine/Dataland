package org.dataland.datalanduserservice.controller

import org.dataland.datalanduserservice.DatalandUserService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest(
    classes = [DatalandUserService::class],
    properties = ["spring.profiles.active=nodb"],
)
@AutoConfigureMockMvc
class PortfolioControllerTest : AbstractPortfolioControllerTest() {
    @Test
    fun `admins can get any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)
        performGetPortfolioAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users cannot get portfolio if they are neither owner nor portfolio is shared`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                eq(portfolioId),
            ),
        ).thenReturn(false)

        performGetPortfolioAndExpect(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `regular users can get portfolio if they are owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                eq(portfolioId),
            ),
        ).thenReturn(false)

        performGetPortfolioAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users can get portfolio if portfolio is shared with them`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                eq(portfolioId),
            ),
        ).thenReturn(true)

        performGetPortfolioAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `admins can create monitored portfolios`() {
        setMockSecurityContext(dummyAdminAuthentication)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `regular users cannot create monitored portfolios if not allowed by rights component`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(false)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `regular users can create monitored portfolios if allowed by rights component`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(true)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `regular users can always create unmonitored portfolios`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(false),
            ),
        ).thenReturn(true)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performCreatePortfolioAndExpect(notMonitoredRequestBody, MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `admins can replace any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)
        doNothing().whenever(companyDataController).isCompanyIdValid(any())
        performReplacePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users cannot replace portfolio they do not own`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(true)
        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performReplacePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `regular users can replace their own portfolio if monitoring manipulation is allowed`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(true)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())

        performReplacePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users cannot replace their own portfolio with monitored flag if not allowed`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(false)

        doNothing().whenever(companyDataController).isCompanyIdValid(any())
        performReplacePortfolioAndExpect(monitoredRequestBody, MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `admins can patch monitoring of any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users cannot patch monitoring if they are not the owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(true)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `regular users can patch monitoring if they are owner and rights component allows it`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(true)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users cannot enable monitoring if rights component forbids it even when owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)
        whenever(
            portfolioRightsUtilsComponent.mayNonAdminUserManipulatePortfolioMonitoring(
                any(),
                eq(true),
            ),
        ).thenReturn(false)

        performPatchMonitoringAndExpect(monitoredPortfolioPatchRequestBody, MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `admins can get all shared portfolios for current user`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performGetSharedPortfoliosAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users can get all shared portfolios for current user`() {
        setMockSecurityContext(dummyUserAuthentication)

        performGetSharedPortfoliosAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `admins can get all shared portfolio names for current user`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performGetSharedPortfolioNamesAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users can get all shared portfolio names for current user`() {
        setMockSecurityContext(dummyUserAuthentication)

        performGetSharedPortfolioNamesAndExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `admins can patch sharing of any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performPatchSharingAndExpect(sharingPatchRequestBody, MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users can patch sharing when they are owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(true)

        performPatchSharingAndExpect(sharingPatchRequestBody, MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `regular users cannot patch sharing when they are not owner`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(portfolioRightsUtilsComponent.isUserPortfolioOwner(portfolioId)).thenReturn(false)

        performPatchSharingAndExpect(sharingPatchRequestBody, MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `admins can delete current user from sharing for any portfolio`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performDeleteCurrentUserFromSharingAndExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    fun `regular users can delete themselves from sharing if portfolio is shared with them`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                eq(portfolioId),
            ),
        ).thenReturn(true)

        performDeleteCurrentUserFromSharingAndExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @Test
    fun `regular users cannot delete themselves from sharing if portfolio is not shared with them`() {
        setMockSecurityContext(dummyUserAuthentication)

        whenever(
            portfolioRightsUtilsComponent.isPortfolioSharedWithUser(
                any(),
                eq(portfolioId),
            ),
        ).thenReturn(false)

        performDeleteCurrentUserFromSharingAndExpect(MockMvcResultMatchers.status().isForbidden)
    }
}
