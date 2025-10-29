package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.services.RequestCreationService
import org.dataland.datasourcingservice.utils.DerivedRightsUtilsComponent
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.util.UUID

class RequestCreationServiceTest {
    private val mockDataSourcingValidator = mock<DataSourcingValidator>()
    private val mockRequestRepository = mock<RequestRepository>()
    private val mockDerivedRightsUtilsComponent = mock<DerivedRightsUtilsComponent>()

    private lateinit var requestCreationService: RequestCreationService

    private val maxNumberOfDailyRequestsForNonPremiumUser = 10

    private val memberUserId = UUID.randomUUID()
    private val nonMemberUserId = UUID.randomUUID()

    private val dummyBasicDataDimensions =
        BasicDataDimensions(
            companyId = UUID.randomUUID().toString(),
            reportingPeriod = "2025",
            dataType = "sfdr",
        )

    @BeforeEach
    fun setup() {
        reset(
            mockDataSourcingValidator,
            mockRequestRepository,
            mockDerivedRightsUtilsComponent,
        )

        doReturn(true).whenever(mockDerivedRightsUtilsComponent).isUserDatalandMember(memberUserId.toString())
        doReturn(false).whenever(mockDerivedRightsUtilsComponent).isUserDatalandMember(nonMemberUserId.toString())
        doReturn(maxNumberOfDailyRequestsForNonPremiumUser)
            .whenever(mockRequestRepository)
            .countByUserIdAndCreationTimestampGreaterThanEqual(any(), any())
        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockRequestRepository).saveAndFlush(any())

        requestCreationService =
            RequestCreationService(
                dataSourcingValidator = mockDataSourcingValidator,
                requestRepository = mockRequestRepository,
                derivedRightsUtilsComponent = mockDerivedRightsUtilsComponent,
                maxRequestsForUser = maxNumberOfDailyRequestsForNonPremiumUser,
            )
    }

    @Test
    fun `check that store request works for a Dataland member regardless of their daily number of requests`() {
        assertDoesNotThrow {
            requestCreationService.storeRequest(
                userId = memberUserId,
                basicDataDimension = dummyBasicDataDimensions,
            )
        }
    }

    @Test
    fun `check that store request throws a QuotaExceededException for a nonmember who exceeded their daily quota`() {
        assertThrows<QuotaExceededException> {
            requestCreationService.storeRequest(
                userId = nonMemberUserId,
                basicDataDimension = dummyBasicDataDimensions,
            )
        }
    }
}
