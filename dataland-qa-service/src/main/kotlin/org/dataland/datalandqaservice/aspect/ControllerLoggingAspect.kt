package org.dataland.datalandqaservice.org.dataland.datalandqaservice.aspect

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.dataland.keycloakAdapter.utils.logAccess
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Class to log access to all REST controllers.
 */
@Aspect
@Component
class ControllerLoggingAspect {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Logs access to all REST controllers.
     */
    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    fun logController() {
        logAccess(logger)
    }
}
