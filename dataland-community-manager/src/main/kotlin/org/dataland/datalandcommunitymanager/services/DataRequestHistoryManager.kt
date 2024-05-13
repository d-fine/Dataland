package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.repositories.RequestStatusRepository
import org.slf4j.LoggerFactory
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
) {

    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    // todo check if other methods could be refactored to this place,
    // i.e. check usage of messageRepository: MessageRepository
    /**
     * Method to store the request status history
     * @param dataRequestStatusHistory list of request status entities
     */
    @Transactional
    fun saveStatusHistory(dataRequestStatusHistory: Set<RequestStatusEntity>) {
        try {
            // todo remove try catch block
            requestStatusRepository.saveAllAndFlush(dataRequestStatusHistory.toList())
        } catch (ex: Exception) {
            dataRequestStatusHistory.forEach {
                logger.info(it.statusHistoryId + " - " + it.dataRequest)
            }
            logger.error("Error while saving status histories: ", ex)
        }
    }

    /**
     * Method to store the request message history
     * @param dataRequestMessageHistory list of request message entities
     */

    @Transactional
    fun saveMessageHistory(dataRequestMessageHistory: List<MessageEntity>) {
        messageRepository.saveAllAndFlush(dataRequestMessageHistory)
    }
}
