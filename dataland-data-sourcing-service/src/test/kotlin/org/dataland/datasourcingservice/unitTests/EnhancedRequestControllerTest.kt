package org.dataland.datasourcingservice.unitTests

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.controller.EnhancedRequestController
import org.dataland.datasourcingservice.model.enhanced.RequestSearchFilter
import org.dataland.datasourcingservice.services.RequestQueryManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset

class EnhancedRequestControllerTest {
    private val mockRequestQueryManager = mock<RequestQueryManager>()

    private lateinit var enhancedRequestController: EnhancedRequestController

    companion object {
        const val UUID_STRING = "00000000-0000-0000-0000-000000000000"
        const val NON_UUID_STRING = "not-a-uuid"
    }

    @BeforeEach
    fun setup() {
        reset(
            mockRequestQueryManager,
        )

        enhancedRequestController =
            EnhancedRequestController(
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
                enhancedRequestController.postRequestSearch(
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
                enhancedRequestController.postRequestCountQuery(
                    requestSearchFilter =
                        RequestSearchFilter<String>(
                            companyId = companyId,
                            userId = userId,
                        ),
                )
            }
        } else {
            assertDoesNotThrow {
                enhancedRequestController.postRequestSearch(
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
                enhancedRequestController.postRequestCountQuery(
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
