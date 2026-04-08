package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.frameworks.sfdr.model.SfdrData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfo
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@DefaultMocks
class MetaDataControllerNonSourceableTest
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val metaDataController: MetaDataController,
        private val companyManager: CompanyAlterationManager,
        private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
    ) {
        private lateinit var companyInfo: CompanyInformation
        private lateinit var companyId: String
        private lateinit var testDataProvider: TestDataProvider
        private val uploaderRoles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER)
        private val adminRoles = DatalandRealmRole.entries.toSet()

        @BeforeEach
        fun setup() {
            testDataProvider = TestDataProvider(objectMapper)
            companyInfo = testDataProvider.getCompanyInformationWithoutIdentifiers(1).first()
            companyId = companyManager.addCompany(companyInfo).companyId
            mockSecurityContext("uploader-user", uploaderRoles)
        }

        @Test
        fun `post non-sourceable with bypass false creates pending inactive canonical entry`() {
            val sourceabilityInfo =
                SourceabilityInfo(
                    companyId = companyId,
                    dataType = DataType.of(SfdrData::class.java),
                    reportingPeriod = "2025",
                    isNonSourceable = true,
                    reason = "No source found",
                )

            metaDataController.postNonSourceabilityOfADataset(false, sourceabilityInfo)

            val created =
                nonSourceabilityDataRepository
                    .findAllByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(
                        companyId = companyId,
                        dataType = DataType.of(SfdrData::class.java),
                        reportingPeriod = "2025",
                    ).first()

            assertEquals(QaStatus.Pending, created.qaStatus)
            assertFalse(created.currentlyActive)
            assertThrows<ResourceNotFoundApiException> {
                metaDataController.isDataNonSourceable(companyId, DataType.of(SfdrData::class.java), "2025")
            }
        }

        @Test
        fun `post non-sourceable with bypass true creates accepted active canonical entry for admins`() {
            mockSecurityContext("admin-user", adminRoles)
            val sourceabilityInfo =
                SourceabilityInfo(
                    companyId = companyId,
                    dataType = DataType.of(SfdrData::class.java),
                    reportingPeriod = "2024",
                    isNonSourceable = true,
                    reason = "Auto accepted claim",
                )

            metaDataController.postNonSourceabilityOfADataset(true, sourceabilityInfo)

            val created =
                nonSourceabilityDataRepository
                    .findAllByCompanyIdAndDataTypeAndReportingPeriodOrderByUploadTimeDesc(
                        companyId = companyId,
                        dataType = DataType.of(SfdrData::class.java),
                        reportingPeriod = "2024",
                    ).first()

            assertEquals(QaStatus.Accepted, created.qaStatus)
            assertTrue(created.currentlyActive)
            assertDoesNotThrow {
                metaDataController.isDataNonSourceable(companyId, DataType.of(SfdrData::class.java), "2024")
            }
        }

        @Test
        fun `get non-sourceable endpoint filters by qaStatus`() {
            mockSecurityContext("admin-user", adminRoles)
            metaDataController.postNonSourceabilityOfADataset(
                false,
                SourceabilityInfo(
                    companyId = companyId,
                    dataType = DataType.of(SfdrData::class.java),
                    reportingPeriod = "2023",
                    isNonSourceable = true,
                    reason = "Needs QA",
                ),
            )
            metaDataController.postNonSourceabilityOfADataset(
                true,
                SourceabilityInfo(
                    companyId = companyId,
                    dataType = DataType.of(SfdrData::class.java),
                    reportingPeriod = "2022",
                    isNonSourceable = true,
                    reason = "Bypassed QA",
                ),
            )

            val pendingEntries =
                metaDataController
                    .getInfoOnNonSourceabilityOfDatasets(
                        companyId = companyId,
                        dataType = DataType.of(SfdrData::class.java),
                        reportingPeriod = null,
                        qaStatus = QaStatus.Pending,
                        nonSourceable = null,
                    ).body!!

            assertTrue(pendingEntries.isNotEmpty())
            assertTrue(pendingEntries.all { it.qaStatus == QaStatus.Pending })
        }

        private fun mockSecurityContext(
            userId: String,
            roles: Set<DatalandRealmRole>,
        ) {
            val authentication = AuthenticationMock.mockJwtAuthentication("mocked-user", userId, roles)
            val securityContext = mock<SecurityContext>()
            whenever(securityContext.authentication).thenReturn(authentication)
            SecurityContextHolder.setContext(securityContext)
        }
    }
