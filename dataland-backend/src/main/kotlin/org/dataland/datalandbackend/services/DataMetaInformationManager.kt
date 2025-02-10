package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
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
    /**
     * Method to associate data information with a specific company
     * @param dataMetaInformation The data meta information which should be stored
     */
    @Transactional
    fun storeDataMetaInformation(dataMetaInformation: DataMetaInformationEntity): DataMetaInformationEntity =
        dataMetaInformationRepositoryInterface.save(dataMetaInformation)

    /**
     * Marks the given dataset as the latest dataset for the combination of dataType, company and reporting period
     * Ensures that only one dataset per group has the active status
     */
    fun setActiveDataset(dataMetaInfo: DataMetaInformationEntity) {
        if (dataMetaInfo.currentlyActive == true) {
            return
        }
        setCurrentlyActiveDatasetInactive(dataMetaInfo.company, dataMetaInfo.dataType, dataMetaInfo.reportingPeriod)
        dataMetaInfo.currentlyActive = true
    }

    /**
     * The method sets the currently active dataset for the triple (company, dataType, reportingPeriod) to inactive in
     * the metadata database
     * @param company the company of the metadata entity to be set to inactive
     * @param dataType the dataType of the metadata entity to be set to inactive
     * @param reportingPeriod the reportingPeriod of the metadata entity to be set to inactive
     */
    fun setCurrentlyActiveDatasetInactive(
        company: StoredCompanyEntity,
        dataType: String,
        reportingPeriod: String,
    ) {
        val metaInfoOfCurrentlyActiveDataset =
            dataMetaInformationRepositoryInterface.getActiveDataset(company, dataType, reportingPeriod)
        if (metaInfoOfCurrentlyActiveDataset != null) {
            metaInfoOfCurrentlyActiveDataset.currentlyActive = null
            dataMetaInformationRepositoryInterface.saveAndFlush(metaInfoOfCurrentlyActiveDataset)
        }
    }

    /**
     * Method to make the data manager get meta info about one specific dataset
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity =
        dataMetaInformationRepositoryInterface.findById(dataId).orElseThrow {
            ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
            )
        }

    /**
     * Method to retrieve the dataset ID of the active dataset for a given set of [dataDimensions]
     * @param dataDimensions the data dimensions for which to retrieve the active dataset ID
     */
    fun getActiveDatasetIdByDataDimensions(dataDimensions: BasicDataDimensions): String? =
        dataMetaInformationRepositoryInterface
            .findActiveDatasetByReportingPeriodAndCompanyIdAndDataType(
                reportingPeriod = dataDimensions.reportingPeriod,
                companyId = dataDimensions.companyId,
                dataType = dataDimensions.dataType,
            )?.dataId

    /**
     * Method to make the data manager search for meta info
     * @param searchFilter contains the filters to be applied for the search
     * @return a list of meta info about data depending on the filters
     */
    fun searchDataMetaInfo(searchFilter: DataMetaInformationSearchFilter): List<DataMetaInformationEntity> {
        searchFilter.companyId?.takeIf { it.isNotBlank() }?.let { companyQueryManager.verifyCompanyIdExists(it) }
        return dataMetaInformationRepositoryInterface.searchDataMetaInformation(searchFilter)
    }

    /**
     * Method to delete the data meta information for a given dataId
     * @param dataId of the dataset that should be deleted
     */
    @Transactional
    fun deleteDataMetaInfo(dataId: String) {
        val dataMetaInformation = getDataMetaInformationByDataId(dataId)
        dataMetaInformationRepositoryInterface.delete(dataMetaInformation)
    }

    /**
     * Queries the meta information for datasets uploaded by a specific user
     * @param userId the id of the user for whom to query data meta information
     * @returns the data meta information uploaded by the specified user
     */
    fun getUserDataMetaInformation(userId: String): List<DataMetaInformationForMyDatasets>? =
        dataMetaInformationRepositoryInterface
            .getUserUploadsDataMetaInfos(userId)
            .map { DataMetaInformationForMyDatasets.fromDatasetMetaInfoEntityForMyDatasets(it) }
}
