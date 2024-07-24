package org.dataland.datalandcommunitymanager.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.repositories.RequestStatusRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Manages all interactions related to the history of Data requests
 */
@Service
class DataRequestHistoryManager(
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val requestStatusRepository: RequestStatusRepository,
    @PersistenceContext private var entityManager: EntityManager,
) {
    /**
     * Method to detach the dataRequestEntity
     * @param dataRequestEntity data request entity
     */
    @Transactional
    fun detachDataRequestEntity(dataRequestEntity: DataRequestEntity) {
        entityManager.detach(dataRequestEntity)
    }

    /**
     * Method to store the request status history
     * @param dataRequestStatusHistory list of request status entities
     */
    @Transactional
    fun saveStatusHistory(dataRequestStatusHistory: List<RequestStatusEntity>) {
        requestStatusRepository.saveAllAndFlush(dataRequestStatusHistory)
    }

    /**
     * Method to store the request message history
     * @param dataRequestMessageHistory list of request message entities
     */
    @Transactional
    fun saveMessageHistory(dataRequestMessageHistory: List<MessageEntity>) {
        messageRepository.saveAllAndFlush(dataRequestMessageHistory)
    }

    /**
     * Persists a new dataRequestStatus to the associated EntityManager.
     */
    @Transactional
    fun persistRequestStatus(dataRequestStatus: RequestStatusEntity) {
        entityManager.persist(dataRequestStatus)
    }

    /**
     * Persists a new message to the associated EntityManager.
     */
    @Transactional
    fun persistMessage(message: MessageEntity) {
        entityManager.persist(message)
    }
}
