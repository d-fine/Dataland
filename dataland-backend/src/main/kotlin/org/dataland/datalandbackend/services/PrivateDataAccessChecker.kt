package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.frameworks.vsme.model.VsmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.infrastructure.ClientException
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service to execute access to private data checks to decide whether a user can access a resource or not
 * @param dataMetaInformationManager required here to find companyIds for dataIds
 * @param requestManager gets the access status for a dataset from the community manager
 */
@Service("PrivateDataAccessChecker")
class PrivateDataAccessChecker(
    @Autowired private val dataMetaInformationManager: DataMetaInformationManager,
    @Autowired private val requestManager: RequestControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Checks whether the user who is currently authenticated has access to a certain dataset.
     * @param dataId the ID of the dataset
     * @return a Boolean indicating whether the user has access or not
     */
    fun hasUserAccessToPrivateResources(dataId: String): Boolean {
        val metaDataEntity = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        val userId = DatalandAuthentication.fromContext().userId
        return try {
            requestManager.hasAccessToDataset(
                UUID.fromString(metaDataEntity.company.companyId),
                metaDataEntity.dataType, metaDataEntity.reportingPeriod,
                UUID.fromString(userId),

            )
            true
        } catch (clientException: ClientException) {
            if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                false
            } else {
                throw clientException
            }
        }
    }

    /**
     * Checks whether the user has currently access to at least on private resource of a company.
     * @param companyId the ID of the company
     * @return a Boolean indicating whether the user has access or not
     */
    fun hasUserAccessToAtLeastOnePrivateResourceForCompany(companyId: String): Boolean {
        val metaDataEntities = dataMetaInformationManager.searchDataMetaInfo(
            companyId = companyId,
            dataType = DataType.of(VsmeData::class.java), showOnlyActive = true, reportingPeriod = null,
        )
        val userId = DatalandAuthentication.fromContext().userId
        metaDataEntities.forEach { metaDataEntity ->
            try {
                requestManager.hasAccessToDataset(
                    UUID.fromString(companyId),
                    metaDataEntity.dataType.toString(), metaDataEntity.reportingPeriod,
                    UUID.fromString(userId),

                )
                return true
            } catch (clientException: ClientException) {
                if (clientException.statusCode == HttpStatus.NOT_FOUND.value()) {
                    logger.info(
                        "User $userId has no access to dataset ${metaDataEntity.dataId} of datatype " +
                            "${metaDataEntity.dataType} for the company ${metaDataEntity.company.companyId}",
                    )
                } else {
                    throw clientException
                }
            }
        }
        return false
    }
}
