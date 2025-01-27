package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandinternalstorage.openApiClient.api.ActuatorApi
import org.dataland.keycloakAdapter.auth.DatalandApiKeyAuthentication
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Triggers the migration of stored datasets to assembled datasets when the backend starts
 */
@Service
class AssembledDataMigrationTrigger
    @Autowired
    constructor(
        private val assembledDataMigrationManager: AssembledDataMigrationManager,
        private val actuatorApi: ActuatorApi,
        private val specificationControllerApi: SpecificationControllerApi,
        private val dataMetaInformationRepository: DataMetaInformationRepository,
    ) {
        companion object {
            const val TRIES_TO_WAIT_FOR_INTERNAL_STORAGE = 10
            const val WAIT_TIME_FOR_INTERNAL_STORAGE = 10_000L
        }

        private val logger = LoggerFactory.getLogger(javaClass)

        private fun waitUntilInternalStorageHealthy() {
            Thread.sleep(WAIT_TIME_FOR_INTERNAL_STORAGE)
            for (attempt in 1..TRIES_TO_WAIT_FOR_INTERNAL_STORAGE) {
                @Suppress("TooGenericExceptionCaught")
                try {
                    actuatorApi.health()
                    return
                } catch (e: Exception) {
                    logger.info("Internal Storage not ready yet. Attempt $attempt/10. Waiting 10 seconds.", e)
                    Thread.sleep(WAIT_TIME_FOR_INTERNAL_STORAGE)
                }
            }
            throw IllegalStateException("Internal Storage not ready after 10 attempts.")
        }

        private fun getAllDataIdsThatNeedToBeMigrated(): List<String> {
            val allFrameworksThatSupportDataPoints = specificationControllerApi.listFrameworkSpecifications().map { it.framework.id }
            val allDatasetsThatNeedToBeMigrated =
                dataMetaInformationRepository
                    .getAllDataMetaInformationThatDoNotHaveDataPoints(allFrameworksThatSupportDataPoints)
            return allDatasetsThatNeedToBeMigrated.map { it.dataId }
        }

        /**
         * Triggers the migration of stored datasets to assembled datasets when the backend starts
         */
        @EventListener
        @Async
        @Transactional
        fun onApplicationEvent(ignored: ContextRefreshedEvent?) {
            logger.info("Looking for stored datasets to migrate to assembled datasets. Waiting until Internal Storage is ready.")
            waitUntilInternalStorageHealthy()
            val dataIdsToMigrate = getAllDataIdsThatNeedToBeMigrated()
            logger.info("Preparing to migrate ${dataIdsToMigrate.size} datasets.")
            SecurityContextHolder.getContext().authentication =
                DatalandApiKeyAuthentication(
                    "internal-api-key",
                    ApiKeyMetaInfo(
                        keycloakUserId = "internal-api-key",
                        keycloakRoles = listOf("ROLE_ADMIN"),
                    ),
                )
            dataIdsToMigrate.forEach {
                @Suppress("TooGenericExceptionCaught")
                try {
                    assembledDataMigrationManager.migrateStoredDatasetToAssembledDataset(it)
                } catch (e: Exception) {
                    logger.error("Migration failed for dataId: $it", e)
                }
            }
        }
    }
