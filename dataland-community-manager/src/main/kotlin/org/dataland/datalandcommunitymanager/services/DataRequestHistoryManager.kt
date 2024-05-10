package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.repositories.RequestStatusRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
/**
 * Manages all interactions related to the history of Data requests
 */
@Service
class DataRequestHistoryManager(
    @Autowired private val messageRepository: MessageRepository,
    @Autowired private val requestStatusRepository: RequestStatusRepository,
) {
    // todo check if other methods could be refactored to this place,
    // i.e. check usage of messageRepository: MessageRepository
    /**
     * Method to store the request status history
     * @param dataRequestStatusHistory list of request status entities
     */
    fun saveStatusHistory(dataRequestStatusHistory: List<RequestStatusEntity>) {
        requestStatusRepository.saveAllAndFlush(dataRequestStatusHistory)
    }

    /**
     * Method to store the request message history
     * @param dataRequestMessageHistory list of request message entities
     */
    fun saveMessageHistory(dataRequestMessageHistory: List<MessageEntity>) {
        messageRepository.saveAllAndFlush(dataRequestMessageHistory)
    }
}
