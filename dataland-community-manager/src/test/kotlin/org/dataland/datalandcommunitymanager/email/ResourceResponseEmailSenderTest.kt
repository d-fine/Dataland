package org.dataland.datalandcommunitymanager.email

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailBuilder
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.readableFrameworkNameMapping
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.slf4j.Logger
import java.util.UUID

class ResourceResponseEmailSenderTest {
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val userId = "1234-221-1111elf"
    private val creationTimestamp = 1709820187875
    private val companyName = "Test Inc."
    private val correlationId = UUID.randomUUID().toString()
    private val staleDaysThreshold = 34
    private val dataTypes = readableFrameworkNameMapping.mapKeys { it.key.value }

    @BeforeEach
    fun setupAuthentication() {
        AuthenticationMock.mockSecurityContext("userEmail", userId, DatalandRealmRole.ROLE_USER)
    }

    private fun getDataRequestEntityWithDataType(dataType: String): DataRequestEntity =
        DataRequestEntity(
            userId = userId,
            dataType = dataType,
            notifyMeImmediately = false,
            reportingPeriod = reportingPeriod,
            datalandCompanyId = companyId,
            creationTimestamp = creationTimestamp,
        )

    private fun getCompanyInfoServiceMock(): CompanyInfoService {
        val companyInfoServiceMock = mock(CompanyInfoService::class.java)
        `when`(companyInfoServiceMock.getValidCompanyNameOrId(companyId))
            .thenReturn(companyName)
        return companyInfoServiceMock
    }

    @Test
    fun `check that the output of the answered request email message sender is correctly built for all frameworks`() {
        dataTypes.forEach {
            val dataRequestEntity = getDataRequestEntityWithDataType(it.key)

            val dataRequestClosedEmailMessageSender =
                DataRequestResponseEmailBuilder(
                    getCompanyInfoServiceMock(),
                    staleDaysThreshold.toString(),
                )
            val mockLogger = mock(Logger::class.java)
            dataRequestClosedEmailMessageSender.logger = mockLogger

            dataRequestClosedEmailMessageSender.buildDataRequestAnsweredEmailAndSendCEMessage(
                dataRequestEntity, correlationId,
            )
            verify(mockLogger, times(1)).info(any())
        }
    }
}
