package org.dataland.datasourcingservice.integrationTests

import org.assertj.core.api.Assertions
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@SpringBootTest(
    classes = [DatalandDataSourcingService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DataSourcingEntityAuditTests
    @Autowired
    constructor(
        private val dataSourcingRepository: DataSourcingRepository,
        private val dataSourcingRevisionRepository: DataRevisionRepository,
    ) : BaseIntegrationTest() {
        @Test
        fun `test audit historization of DataSourcingEntity with updated states`() {
            val dataSourcingEntityId = UUID.randomUUID()
            val companyId = UUID.randomUUID()
            val initialDate = LocalDate.now(ZoneId.systemDefault())
            val initialAdminComment = "Initialized data sourcing"
            val initialState = DataSourcingState.Initialized

            val entity =
                DataSourcingEntity(
                    dataSourcingId = dataSourcingEntityId,
                    companyId = companyId,
                    reportingPeriod = "2023",
                    dataType = "sfdr",
                    state = initialState,
                    documentIds = setOf(),
                    expectedPublicationDatesOfDocuments = setOf(),
                    dateOfNextDocumentSourcingAttempt = initialDate,
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

            Assertions.assertThat(entityRevisionList).hasSize(3) // Create, Update 1, Update 2
            Assertions.assertThat(entityRevisionList[0].state).isEqualTo(initialState)
            Assertions.assertThat(entityRevisionList[0].adminComment).isEqualTo(initialAdminComment)
            Assertions.assertThat(entityRevisionList[1].state).isEqualTo(DataSourcingState.DocumentSourcing)
            Assertions.assertThat(entityRevisionList[1].adminComment).isEqualTo(updatedAdminComment1)
            Assertions.assertThat(entityRevisionList[2].state).isEqualTo(DataSourcingState.DataVerification)
            Assertions.assertThat(entityRevisionList[2].adminComment).isEqualTo(updatedAdminComment2)
        }

        @AfterEach
        fun cleanup() {
            dataSourcingRepository.deleteAll()
        }
    }
