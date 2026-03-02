package org.dataland.datasourcingservice.integrationTests.serviceTests

import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.COMPANY_ID_2
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_1
import org.dataland.datasourcingservice.utils.DATA_SOURCING_STATE_2
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_2
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.datasourcingservice.utils.DerivedRightsUtilsComponent
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_2
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

@SpringBootTest(
    classes = [DatalandDataSourcingService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class DataSourcingQueryManagerTest
    @Autowired
    constructor(
        private val dataSourcingQueryManager: DataSourcingQueryManager,
        private val dataSourcingRepository: DataSourcingRepository,
        private val derivedRightsUtilsComponent: DerivedRightsUtilsComponent,
    ) : BaseIntegrationTest() {
        @MockBean
        lateinit var companyRolesControllerApi: CompanyRolesControllerApi
        private val dataBaseCreationUtils = DataBaseCreationUtils(dataSourcingRepository = dataSourcingRepository)
        private lateinit var dataSourcingEntities: List<DataSourcingEntity>
        private val mockSecurityContext = mock<SecurityContext>()
        private lateinit var mockAuthentication: DatalandAuthentication

        /**
         * Setting the security context to use the specified userId and set of roles.
         */
        private fun resetSecurityContext(isUserAdmin: Boolean) {
            mockAuthentication =
                AuthenticationMock.mockJwtAuthentication(
                    "userName",
                    "e2a9f1a2-5b3c-4d6e-8f7a-1b2c3d4e5f60",
                    if (isUserAdmin) setOf(DatalandRealmRole.ROLE_ADMIN) else setOf(DatalandRealmRole.ROLE_USER),
                )
            doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
            SecurityContextHolder.setContext(mockSecurityContext)
            doReturn(emptyList<Any>()).whenever(companyRolesControllerApi).getExtendedCompanyRoleAssignments(
                anyOrNull(), anyOrNull(), anyOrNull(),
            )
        }

        /**
         * Store 8 data sourcings covering all combinations of the three filter parameters other than state.
         * Note: i / 2^k % 2 is the position k binary digit of i, with k=0 for the least significant bit.
         * Roughly one third of the data sourcings have state Initialized, the rest have state DocumentSourcing.
         */
        @BeforeEach
        fun setup() {
            dataSourcingEntities =
                (0..7).map {
                    dataBaseCreationUtils.storeDataSourcing(
                        companyId =
                            if (it / 4 % 2 == 0) {
                                UUID.fromString(COMPANY_ID_1)
                            } else {
                                UUID.fromString(
                                    COMPANY_ID_2,
                                )
                            },
                        dataType = if (it / 2 % 2 == 0) DATA_TYPE_1 else DATA_TYPE_2,
                        reportingPeriod = if (it % 2 == 0) REPORTING_PERIOD_1 else REPORTING_PERIOD_2,
                        state = DataSourcingState.valueOf(if (it % 3 == 0) DATA_SOURCING_STATE_1 else DATA_SOURCING_STATE_2),
                    )
                }
        }

        @ParameterizedTest
        @CsvSource(
            value = [
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, ${DATA_SOURCING_STATE_1}, 0, true",
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, null, 0, true",
                "null, null, null, ${DATA_SOURCING_STATE_1}, 0;3;6, true",
                "null, null, null, null, 0;1;2;3;4;5;6;7, true",
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, ${DATA_SOURCING_STATE_1}, 0, false",
                "${COMPANY_ID_1}, ${DATA_TYPE_1}, ${REPORTING_PERIOD_1}, null, 0, false",
                "null, null, null, ${DATA_SOURCING_STATE_1}, 0;3;6, false",
                "null, null, null, null, 0;1;2;3;4;5;6;7, false",
            ],
            nullValues = ["null"],
        )
        fun `ensure that searching for data sourcings works as intended`(
            companyId: String?,
            dataType: String?,
            reportingPeriod: String?,
            dataSourcingState: String?,
            indexString: String,
            isUserAdmin: Boolean,
        ) {
            resetSecurityContext(isUserAdmin)
            val indicesOfExpectedResults = indexString.split(';').map { it.toInt() }
            val expectedResults =
                indicesOfExpectedResults
                    .map { dataSourcingEntities[it].toStoredDataSourcing(derivedRightsUtilsComponent) }
            val actualResults =
                dataSourcingQueryManager.searchDataSourcings(
                    companyId = companyId?.let { UUID.fromString(it) },
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    state = dataSourcingState?.let { DataSourcingState.valueOf(it) },
                )
            Assertions.assertEquals(expectedResults.size, actualResults.size)
            expectedResults.forEach {
                assert(it in actualResults) { "Expected result $it not found in actual results" }
            }
        }

        @Test
        fun `ensure that non admin users cannot see sensitive fields`() {
            resetSecurityContext(isUserAdmin = false)
            dataSourcingQueryManager
                .searchDataSourcings(
                    companyId = null,
                    dataType = null,
                    reportingPeriod = null,
                    state = null,
                ).forEach {
                    Assertions.assertNull(it.adminComment, "Non-admin user should not see adminComment")
                    Assertions.assertTrue(
                        it.associatedRequestIds.isEmpty(),
                        "Non-admin user should not see associatedRequestIds",
                    )
                    Assertions.assertNull(it.priority, "Non-admin non-provider user should not see priority")
                }
        }
    }
