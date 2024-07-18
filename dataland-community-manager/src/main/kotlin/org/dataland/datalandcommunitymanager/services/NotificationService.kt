package org.dataland.datalandcommunitymanager.services

import org.springframework.stereotype.Service

@Service("NotificationService")
class NotificationService {

    fun triggerNotificationEvent(): Boolean {
        //TODO
        return false
    }

    fun sendSingleEmailMessageToQueue() {}

    fun sendSummaryEmailMessageToQueue() {}

    fun createNotificationEntry() {}
}