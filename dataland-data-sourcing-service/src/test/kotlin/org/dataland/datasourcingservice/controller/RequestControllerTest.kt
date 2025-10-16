package org.dataland.datasourcingservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.dataland.datasourcingservice.services.BulkRequestManager
import org.dataland.datasourcingservice.services.ExistingRequestsManager
import org.dataland.datasourcingservice.services.RequestCreationService
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset

class RequestControllerTest {
    private val mockExistingRequestsManager = mock<ExistingRequestsManager>()
    private val mockBulkRequestManager = mock<BulkRequestManager>()
    private val mockRequestCreationService = mock<RequestCreationService>()
    private val mockRequestQueryManager = mock<RequestQueryManager>()

    private lateinit var requestController: RequestController

    companion object {
        const val UUID_STRING = "00000000-0000-0000-0000-000000000000"
        const val NON_UUID_STRING = "not-a-uuid"
    }

    @BeforeEach
    fun setup() {
        reset(
            mockExistingRequestsManager,
            mockBulkRequestManager,
            mockRequestCreationService,
            mockRequestQueryManager,
        )

        requestController =
            RequestController(
                existingRequestsManager = mockExistingRequestsManager,
                bulkDataRequestManager = mockBulkRequestManager,
                requestCreationService = mockRequestCreationService,
                requestQueryManager = mockRequestQueryManager,
            )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "$NON_UUID_STRING, null",
            "null, $NON_UUID_STRING",
            "null, null",
            "$UUID_STRING, $UUID_STRING",

        ],
        nullValues = ["null"],
    )
    fun `verify that UUID validation behaves as expected in postRequestSearch and postRequestCountQuery`(
        companyId: String?,
        userId: String?,
    ) {
        if (companyId == NON_UUID_STRING || userId == NON_UUID_STRING) {
            assertThrows<ResourceNotFoundApiException> {
                requestController.postRequestSearch(
                    requestSearchFilter =
                        RequestSearchFilter<String>(
                            companyId = companyId,
                            userId = userId,
                        ),
                    chunkSize = 100,
                    chunkIndex = 0,
                )
            }
            assertThrows<ResourceNotFoundApiException> {
                requestController.postRequestCountQuery(
                    requestSearchFilter =
                        RequestSearchFilter<String>(
                            companyId = companyId,
                            userId = userId,
                        ),
                )
            }
        } else {
            assertDoesNotThrow {
                requestController.postRequestSearch(
                    requestSearchFilter =
                        RequestSearchFilter<String>(
                            companyId = companyId,
                            userId = userId,
                        ),
                    chunkSize = 100,
                    chunkIndex = 0,
                )
            }
            assertDoesNotThrow {
                requestController.postRequestCountQuery(
                    requestSearchFilter =
                        RequestSearchFilter<String>(
                            companyId = companyId,
                            userId = userId,
                        ),
                )
            }
        }
    }
}
