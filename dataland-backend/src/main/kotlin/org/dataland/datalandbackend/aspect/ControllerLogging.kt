package org.dataland.datalandbackend.aspect

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.dataland.keycloakAdapter.utils.logAccess
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class ControllerLoggingAspect() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    fun logController() {
        logAccess(logger)
    }
}
