package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Component("DataMetaInformationManager")
class DataMetaInformationManager(
    @Autowired private val dataMetaInformationRepositoryInterface: DataMetaInformationRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to associate data information with a specific company
     * @param dataMetaInformation The data meta information which should be stored
     */
    fun storeDataMetaInformation(
        dataMetaInformation: DataMetaInformationEntity,
    ): DataMetaInformationEntity {
        return dataMetaInformationRepositoryInterface.save(dataMetaInformation)
    }

    /**
     * Marks the given dataset as the latest dataset for the combination of dataType, company and reporting period
     * Ensures that only one dataset per group has the active status
     */
    fun setActiveDataset(dataMetaInfo: DataMetaInformationEntity) {
        if (dataMetaInfo.currentlyActive == true) {
            return
        }
        setNewDatasetActiveAndOldDatasetInactive(dataMetaInfo)
    }

    /**
     * The method sets a new dataset active in the metadata database and sets the existing dataset to inactive
     * @param dataMetaInfo the DataMetaInformationEntity of the dataset
     */
    fun setNewDatasetActiveAndOldDatasetInactive(dataMetaInfo: DataMetaInformationEntity) {
        val metaInfoOfCurrentlyActiveDataset = dataMetaInformationRepositoryInterface.getActiveDataset(
            dataMetaInfo.company,
            dataMetaInfo.dataType,
            dataMetaInfo.reportingPeriod,
        )
        if (metaInfoOfCurrentlyActiveDataset != null) {
            metaInfoOfCurrentlyActiveDataset.currentlyActive = null
            dataMetaInformationRepositoryInterface.saveAndFlush(metaInfoOfCurrentlyActiveDataset)
        }
        dataMetaInfo.currentlyActive = true
    }

    /**
     * Method to make the data manager get meta info about one specific data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity {
        return dataMetaInformationRepositoryInterface.findById(dataId).orElseThrow {
            ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
            )
        }
    }

    /**
     * Method to make the data manager search for meta info
     * @param companyId if not empty, it filters the requested meta info to a specific company
     * @param dataType if not empty, it filters the requested meta info to a specific data type
     * @param reportingPeriod if not empty, it filters the requested meta info to a specific reporting period
     * @param showOnlyActive if true, it will only return datasets marked "active"
     * @return a list of meta info about data depending on the filters
     */
    fun searchDataMetaInfo(
        companyId: String,
        dataType: String?,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): List<DataMetaInformationEntity> {
        /*if (companyId != "") {
            companyQueryManager.verifyCompanyIdExists(companyId)
        }*/
        val dataTypeFilter = dataType ?: ""
        logger.info("dataTypeFilter: $dataTypeFilter")
        val reportingPeriodFilter = reportingPeriod ?: ""
        val filter = DataMetaInformationSearchFilter(
            companyIdFilter = companyId,
            dataTypeFilter = dataTypeFilter,
            reportingPeriodFilter = reportingPeriodFilter,
            onlyActive = showOnlyActive,
        )

        return dataMetaInformationRepositoryInterface.searchDataMetaInformation(filter)
    }

    /**
     * Method to delete the data meta information for a given dataId
     * @param dataId of the dataset that should be deleted
     */
    fun deleteDataMetaInfo(
        dataId: String,
    ) {
        val dataMetaInformation = getDataMetaInformationByDataId(dataId)
        dataMetaInformationRepositoryInterface.delete(dataMetaInformation)
    }

    /**
     * Queries the meta information for datasets uploaded by a specific user
     * @param userId the id of the user for whom to query data meta information
     * @returns the data meta information uploaded by the specified user
     */
    fun getUserDataMetaInformation(userId: String): List<DataMetaInformationForMyDatasets>? {
        return dataMetaInformationRepositoryInterface.getUserUploadsDataMetaInfos(userId)
            .map { DataMetaInformationForMyDatasets.fromDatasetMetaInfoEntityForMyDatasets(it) }
    }
}
