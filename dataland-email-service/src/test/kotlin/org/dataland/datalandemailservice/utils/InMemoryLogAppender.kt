package org.dataland.datalandemailservice.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.LoggerFactory

class InMemoryLogAppender : AppenderBase<ILoggingEvent>() {
    private val log = mutableListOf<ILoggingEvent>()

    override fun append(eventObject: ILoggingEvent) {
        log.add(eventObject)
    }

    fun contains(
        message: String,
        level: Level,
    ): Boolean = log.any { it.message.contains(message) && it.level == level }

    fun getAppender(): InMemoryLogAppender {
        val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        logger.addAppender(this)
        this.start()
        return this
    }
}
