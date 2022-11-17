package org.dataland.datalandbackend.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("authorisationService")
class AuthorisationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    fun isAuthorised(): Boolean {
        logger.info("Are you authorised?")
        return true
    }
}
