package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.repositories.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Provides utility functions to be used in PreAuthorize blocks.
 */
@Service("SecurityUtilsService")
class SecurityUtilsService
@Autowired
constructor(
    private val requestRepository: RequestRepository,
) {
    /**
     * Returns true if and only if the currently authenticated user is asking for his/her own request
     */
    @Transactional(readOnly = true)
    fun isUserAskingForOwnRequest(requestId: String): Boolean {
        val userIdOfRequest = requestRepository.findById(UUID.fromString(requestId)).getOrNull()?.userId ?: return false
        val userIdRequester = UUID.fromString(SecurityContextHolder.getContext().authentication.name)
        return userIdOfRequest == userIdRequester
    }
}
