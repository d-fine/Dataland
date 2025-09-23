package org.dataland.datasourcingservice

import org.assertj.core.api.Assertions.assertThat
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.ZoneId
import java.util.Date
import java.util.UUID

@SpringBootTest
@Testcontainers
class DataSourcingEntityAuditTests : BaseIntegrationTest() {
    @Autowired
    private lateinit var dataSourcingRepository: DataSourcingRepository

    @Autowired
    private lateinit var dataSourcingRevisionRepository: DataRevisionRepository

    @Test
    fun `test audit historization of DataSourcingEntity with updated states`() {
        val dataSourcingEntityId = UUID.randomUUID()
        val companyId = UUID.randomUUID()
        val initialDate = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val initialAdminComment = "Initialized data sourcing"
        val initialState = DataSourcingState.Initialized

        val entity =
            DataSourcingEntity(
                id = dataSourcingEntityId,
                companyId = companyId,
                reportingPeriod = "Q1-2023",
                dataType = "SFDR",
                state = initialState,
                documentIds = setOf(),
                expectedPublicationDatesOfDocuments = setOf(),
                dateDocumentSourcingAttempt = initialDate,
                documentCollector = UUID.randomUUID(),
                dataExtractor = UUID.randomUUID(),
                adminComment = initialAdminComment,
                associatedRequests = mutableSetOf(),
            )
        dataSourcingRepository.saveAndFlush(entity)

        val updatedState1 = DataSourcingState.DocumentSourcing
        val updatedAdminComment1 = "Document sourcing in progress"
        entity.apply {
            state = updatedState1
            adminComment = updatedAdminComment1
        }
        dataSourcingRepository.saveAndFlush(entity)

        val updatedState2 = DataSourcingState.DataVerification
        val updatedAdminComment2 = "Data verification stage"
        entity.apply {
            state = updatedState2
            adminComment = updatedAdminComment2
        }
        dataSourcingRepository.saveAndFlush(entity)

        val entityRevisionList =
            dataSourcingRevisionRepository
                .listDataSourcingRevisionsById(dataSourcingEntityId)

        assertThat(entityRevisionList).hasSize(3) // Create, Update 1, Update 2
        assertThat(entityRevisionList[0].state).isEqualTo(initialState)
        assertThat(entityRevisionList[0].adminComment).isEqualTo(initialAdminComment)
        assertThat(entityRevisionList[1].state).isEqualTo(DataSourcingState.DocumentSourcing)
        assertThat(entityRevisionList[1].adminComment).isEqualTo(updatedAdminComment1)
        assertThat(entityRevisionList[2].state).isEqualTo(DataSourcingState.DataVerification)
        assertThat(entityRevisionList[2].adminComment).isEqualTo(updatedAdminComment2)
    }
}
