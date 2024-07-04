package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service("SecurityUtilsService")
class SecurityUtilsService {

    @Transactional // TODO Braucht man das?
    fun isUserAskingQaReviewStatusOfUploadedDataset(identifier: UUID): Boolean {
        // GET /metadata/{dataId}
        // check if user id is the same
        return true
    }
}
