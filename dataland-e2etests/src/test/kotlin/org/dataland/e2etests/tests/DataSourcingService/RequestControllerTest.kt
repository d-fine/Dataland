
import org.dataland.dataSourcingService.openApiClient.model.DataRequest
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SpringBootTest
@AutoConfigureWebTestClient
class RequestControllerTest {
    private val apiAccessor = ApiAccessor()

    @Test
    fun `post a request and verify that it can be retrieved`() {
        val timeBeforeUpload = OffsetDateTime.now(ZoneOffset.UTC)
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dummyRequest = DataRequest(companyId, "sfdr", "2023", "dummy request")
        val requestId = apiAccessor.dataSourcingRequestControllerApi.createRequest(dummyRequest).id

        val storedRequest = apiAccessor.dataSourcingRequestControllerApi.getRequest(requestId)

        assertEquals(dummyRequest.companyIdentifier, storedRequest.companyId)
        assertEquals(dummyRequest.dataType, storedRequest.dataType)
        assertEquals(dummyRequest.reportingPeriod, storedRequest.reportingPeriod)
        assertEquals(dummyRequest.comment, storedRequest.memberComment)

        assertTrue(timeBeforeUpload < storedRequest.creationTimeStamp)
        assertTrue(storedRequest.creationTimeStamp < OffsetDateTime.now(ZoneOffset.UTC))
        assertEquals(storedRequest.creationTimeStamp, storedRequest.lastModifiedDate)
        assertEquals(RequestState.Open, storedRequest.state)
    }

    @Test
    fun `post a request with invalid company ID and verify that it is rejected`() {
        val invalidRequest = DataRequest("invalidCompanyId", "sfdr", "2023", "dummy request")
        assertThrows<ResourceNotFoundApiException> {
            apiAccessor.dataSourcingRequestControllerApi.createRequest(invalidRequest)
        }
    }

    @Test
    fun `patch a request and verify that the changes are saved`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val dummyRequest = DataRequest(companyId, "sfdr", "2023", "dummy request")
        val initialRequest = apiAccessor.dataSourcingRequestControllerApi.createRequest(dummyRequest)

        apiAccessor.dataSourcingRequestControllerApi.patchDataRequestState(initialRequest.id, RequestState.Processing)
        val patchedRequest = apiAccessor.dataSourcingRequestControllerApi.getRequest(initialRequest.id)

        assertTrue(patchedRequest.lastModifiedDate > initialRequest.lastModifiedDate)
        assertEquals(RequestState.Processing, patchedRequest.state)
        assertEquals(
            initialRequest.copy(state = RequestState.Processing, lastModifiedDate = patchedRequest.lastModifiedDate),
            patchedRequest,
        )
    }
}
