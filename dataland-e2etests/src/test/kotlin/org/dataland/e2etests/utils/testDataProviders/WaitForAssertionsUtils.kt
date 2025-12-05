package org.dataland.e2etests.utils.testDataProviders

import org.awaitility.Awaitility
import java.util.concurrent.TimeUnit

internal fun awaitUntilAsserted(operation: () -> Any) =
    Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).untilAsserted {
        operation()
    }

internal fun awaitUntil(operation: () -> Boolean) =
    Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).until {
        operation()
    }
