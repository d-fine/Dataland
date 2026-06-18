package org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DataPointQaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementCreationService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementFinalizationService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementSupportService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.SignificanceCheckService
import org.dataland.datalandqaservice.utils.MockDatasetJudgementEntityForTest
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

open class DatasetJudgementServiceTestBase {
    protected val datasetJudgementRepository = mock<DatasetJudgementRepository>()
    protected val datasetJudgementSupportService = mock<DatasetJudgementSupportService>()
    protected val keycloakUserService = mock<KeycloakUserService>()
    protected val datasetJudgementFinalizationService =
        DatasetJudgementFinalizationService(
            mock<DataPointControllerApi>(),
            mock<DataPointQaReviewManager>(),
            mock<QaReviewManager>(),
        )

    protected val creationServiceClass =
        DatasetJudgementCreationService(
            datasetJudgementSupportService,
            keycloakUserService,
            PreApprovalService(
                autoPreApprovalEnabled = false,
                exemptFieldsConfig = PreApprovalExemptFieldsConfig(),
                significanceCheckService = SignificanceCheckService(),
                datasetJudgementSupportService = datasetJudgementSupportService,
            ),
        )

    protected val service =
        DatasetJudgementService(
            datasetJudgementRepository,
            datasetJudgementSupportService,
            creationServiceClass,
            datasetJudgementFinalizationService,
        )

    protected val mockDatasetJudgementEntityForTest = MockDatasetJudgementEntityForTest
    protected val datasetJudgementEntity = mockDatasetJudgementEntityForTest.createDummyDatasetJudgementEntity()
    protected val dummyMetaData = mockDatasetJudgementEntityForTest.createDummyMetaData()

    @BeforeEach
    fun setup() {
        reset(
            datasetJudgementRepository,
            datasetJudgementSupportService,
        )

        AuthenticationMock.mockSecurityContext(
            "data.admin@example.com",
            mockDatasetJudgementEntityForTest.dummyUserId.toString(),
            setOf(DatalandRealmRole.ROLE_ADMIN),
        )

        doReturn(datasetJudgementEntity)
            .whenever(datasetJudgementSupportService)
            .getDatasetJudgementEntityById(any())

        whenever(datasetJudgementRepository.save(any<DatasetJudgementEntity>()))
            .thenAnswer { it.arguments[0] as DatasetJudgementEntity }

        doReturn(
            KeycloakUserInfo(
                mockDatasetJudgementEntityForTest.DUMMY_USER_EMAIL,
                mockDatasetJudgementEntityForTest.dummyUserId.toString(),
                mockDatasetJudgementEntityForTest.DUMMY_USER_FIRST_NAME,
                mockDatasetJudgementEntityForTest.DUMMY_USER_LAST_NAME,
            ),
        ).whenever(keycloakUserService)
            .getUser(any())
    }

    protected fun captureSavedJudgement(): DatasetJudgementEntity {
        val captor = argumentCaptor<DatasetJudgementEntity>()
        verify(datasetJudgementRepository).save(captor.capture())
        return captor.firstValue
    }
}
