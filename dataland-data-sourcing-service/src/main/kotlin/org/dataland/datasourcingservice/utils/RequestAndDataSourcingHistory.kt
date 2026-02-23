package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences

/**
 * Holds request and data sourcing history lists together.
 */
data class RequestAndDataSourcingHistory(
    val requestHistory: List<RequestEntity>,
    val dataSourcingHistory: List<DataSourcingWithoutReferences>,
)
