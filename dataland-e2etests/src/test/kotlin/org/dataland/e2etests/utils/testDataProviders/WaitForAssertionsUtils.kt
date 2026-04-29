package org.dataland.e2etests.utils.testDataProviders

import org.awaitility.Awaitility
import java.util.concurrent.TimeUnit

internal fun awaitUntilAsserted(
    timeoutSeconds: Long = 2,
    operation: () -> Any,
) = Awaitility
    .await()
    .atMost(timeoutSeconds, TimeUnit.SECONDS)
    .pollDelay(500, TimeUnit.MILLISECONDS)
    .pollInterval(1, TimeUnit.SECONDS)
    .untilAsserted {
        operation()
    }

internal fun awaitUntil(
    timeoutSeconds: Long = 2,
    operation: () -> Boolean,
) = Awaitility
    .await()
    .atMost(timeoutSeconds, TimeUnit.SECONDS)
    .pollDelay(500, TimeUnit.MILLISECONDS)
    .pollInterval(1, TimeUnit.SECONDS)
    .until {
        operation()
    }
