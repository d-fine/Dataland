package org.dataland.datalandbackend.configurations

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync

/**
 * Configuration class to enable asynchronous processing in the application.
 * This allows methods annotated with @Async to be executed in a separate thread.
 */
@Configuration
@EnableAsync
class AsyncConfig
