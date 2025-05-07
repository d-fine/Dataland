package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.services.RequestEmailManager
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.Instant

class DataRequestUpdateUtilsTest {
    companion object {
        private val testDataProvider = DataRequestUpdateManagerTestDataProvider()

        @JvmStatic
        fun provideParametersToCheckNotificationFlagResetBehavior() = testDataProvider.getStreamOfArgumentsToTestFlagResetBehavior()
    }

    private lateinit var dataRequestUpdateUtils: DataRequestUpdateUtils
    private val mockDataRequestProcessingUtils = mock<DataRequestProcessingUtils>()
    private val mockDataRequestLogger = mock<DataRequestLogger>()
    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val mockRequestEmailManager = mock<RequestEmailManager>()
    private val mockQaControllerApi = mock<QaControllerApi>()

    private lateinit var dummyDataRequestEntity: DataRequestEntity

    private val dummyEmail = "test@example.com"
    private val dummyMessage = "dummy message"
    private val dummyAdminComment = "test comment"

    @BeforeEach
    fun setup() {
        TestUtils.mockSecurityContext("user@example.com", "1234-221-1111elf", DatalandRealmRole.ROLE_USER)
        dummyDataRequestEntity =
            DataRequestEntity(
                userId = "",
                dataType = "p2p",
                notifyMeImmediately = true,
                reportingPeriod = "",
                datalandCompanyId = "",
                creationTimestamp = 0L,
            )
        reset(
            mockDataRequestProcessingUtils,
            mockDataRequestLogger,
            mockCompanyInfoService,
            mockCompanyRolesManager,
            mockRequestEmailManager,
            mockQaControllerApi,
        )
        dataRequestUpdateUtils =
            DataRequestUpdateUtils(
                mockDataRequestProcessingUtils,
                mockDataRequestLogger,
                mockCompanyInfoService,
                mockCompanyRolesManager,
                mockRequestEmailManager,
                mockQaControllerApi,
            )
    }

    @Test
    fun `validate that a patch of the admin comment only does not register as a change of the data request entity`() {
        val dataRequestPatch = DataRequestPatch(adminComment = dummyAdminComment)
        val currentTime = Instant.now().toEpochMilli()

        assertFalse(
            dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateRequestStatusHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime, null,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateMessageHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )
    }

    @Test
    fun `validate that the modification time changes if the request priority is patched`() {
        val dataRequestPatch = DataRequestPatch(requestPriority = RequestPriority.High)
        val currentTime = Instant.now().toEpochMilli()

        assertFalse(
            dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateRequestStatusHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime, null,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateMessageHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime,
            ),
        )
        assertTrue(
            dataRequestUpdateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )
    }

    @Test
    fun `validate that the sending of a request email is triggered when a request message is added`() {
        val dataRequestPatch =
            DataRequestPatch(
                contacts = setOf(dummyEmail),
                message = dummyMessage,
            )
        val currentTime = Instant.now().toEpochMilli()

        assertFalse(
            dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateRequestStatusHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime, null,
            ),
        )
        assertTrue(
            dataRequestUpdateUtils.updateMessageHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )

        verify(mockRequestEmailManager, times(1))
            .sendSingleDataRequestEmail(
                any(), any<Set<String>>(), any<String>(),
            )
    }

    @Test
    fun `validate that no email is sent when both request priority and admin comment are patched`() {
        val dataRequestPatch =
            DataRequestPatch(
                requestPriority = RequestPriority.High,
                adminComment = dummyAdminComment,
            )
        val currentTime = Instant.now().toEpochMilli()

        assertFalse(
            dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateRequestStatusHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime, null,
            ),
        )
        assertFalse(
            dataRequestUpdateUtils.updateMessageHistoryIfRequired(
                dataRequestPatch, dummyDataRequestEntity, currentTime,
            ),
        )
        assertTrue(
            dataRequestUpdateUtils.checkPriorityAndAdminCommentChangesAndLogPatchMessagesIfRequired(
                dataRequestPatch, dummyDataRequestEntity,
            ),
        )

        verify(mockRequestEmailManager, times(0))
            .sendSingleDataRequestEmail(
                any(), any<Set<String>>(), any<String>(),
            )
    }

    @ParameterizedTest
    @MethodSource("provideParametersToCheckNotificationFlagResetBehavior")
    fun `validate that the notification flag is set from true to false if and only if requestStatus changes`(
        requestStatusBefore: RequestStatus,
        requestStatusAfter: RequestStatus,
    ) {
        val dataRequestPatch = DataRequestPatch(requestStatus = requestStatusAfter)

        dummyDataRequestEntity.dataRequestStatusHistory =
            listOf(
                RequestStatusEntity(
                    StoredDataRequestStatusObject(
                        status = requestStatusBefore,
                        creationTimestamp = 1L,
                        accessStatus = AccessStatus.Public,
                        requestStatusChangeReason = null,
                        answeringDataId = null,
                    ),
                    mock<DataRequestEntity>(),
                ),
            )

        dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(dataRequestPatch, dummyDataRequestEntity)

        if (requestStatusBefore == requestStatusAfter) {
            assertTrue(dummyDataRequestEntity.notifyMeImmediately)
        } else {
            assertFalse(dummyDataRequestEntity.notifyMeImmediately)
        }
    }

    @ParameterizedTest
    @MethodSource("provideParametersToCheckNotificationFlagResetBehavior")
    fun `validate that the notification flag remains true under changes of requestStatus if set true by patch`(
        requestStatusBefore: RequestStatus,
        requestStatusAfter: RequestStatus,
    ) {
        val dataRequestPatch = DataRequestPatch(requestStatus = requestStatusAfter, notifyMeImmediately = true)

        dummyDataRequestEntity.dataRequestStatusHistory =
            listOf(
                RequestStatusEntity(
                    StoredDataRequestStatusObject(
                        status = requestStatusBefore,
                        creationTimestamp = 1L,
                        accessStatus = AccessStatus.Public,
                        requestStatusChangeReason = null,
                        answeringDataId = null,
                    ),
                    mock<DataRequestEntity>(),
                ),
            )

        dataRequestUpdateUtils.updateNotifyMeImmediatelyIfRequired(dataRequestPatch, dummyDataRequestEntity)

        assertTrue(dummyDataRequestEntity.notifyMeImmediately)
    }
}
