package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Manages requests corresponding to a non-sourceable dataset
 */
@Service
class NonSourceableDataManager(
    @Autowired private val dataRequestUpdateManager: DataRequestUpdateManager,
    @Autowired private val dataRequestRepository: DataRequestRepository,
)
