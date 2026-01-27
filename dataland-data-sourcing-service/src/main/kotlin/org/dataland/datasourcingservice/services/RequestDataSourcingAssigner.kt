package org.dataland.datasourcingservice.services

import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service class responsible for assigning requests to data sourcing entities.
 */
@Service
class RequestDataSourcingAssigner
    @Autowired
    constructor(
        private val dataSourcingRepository: DataSourcingRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Resets an existing DataSourcingEntity to the Initialized state or creates a new one if none exists.
         *
         * Associates the given RequestEntity with the DataSourcingEntity and stores it in the database. This will also
         * cascade the save operation to the associated RequestEntity automatically.
         *
         * @param requestEntity the RequestEntity to associate with the DataSourcingEntity
         * @return the reset or newly created DataSourcingEntity
         */
        fun useExistingOrCreateDataSourcingAndAddRequest(requestEntity: RequestEntity): DataSourcingEntity {
            val dataSourcingEntity =
                dataSourcingRepository.findByDataDimensionAndFetchAllStoredFields(
                    requestEntity.companyId,
                    requestEntity.dataType,
                    requestEntity.reportingPeriod,
                ) ?: DataSourcingEntity(
                    companyId = requestEntity.companyId,
                    reportingPeriod = requestEntity.reportingPeriod,
                    dataType = requestEntity.dataType,
                )
            logger.info(
                "Add request with id ${requestEntity.id} to data sourcing entity with id ${dataSourcingEntity.dataSourcingId}.",
            )
            if (dataSourcingEntity.state in setOf(DataSourcingState.Done, DataSourcingState.NonSourceable)) {
                dataSourcingEntity.state = DataSourcingState.Initialized
            }
            dataSourcingEntity.addAssociatedRequest(requestEntity)
            return dataSourcingRepository.save(dataSourcingEntity)
        }
    }
