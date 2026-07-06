package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataMetaInformationForMyDatasets
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Component("DataMetaInformationManager")
class DataMetaInformationManager(
    @Autowired private val dataMetaInformationRepository: DataMetaInformationRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    /**
     * Method to associate data information with a specific company
     * @param dataMetaInformation The data meta information which should be stored
     */
    @Transactional
    fun storeDataMetaInformation(dataMetaInformation: DataMetaInformationEntity): DataMetaInformationEntity =
        dataMetaInformationRepository.save(dataMetaInformation)

    /**
     * Marks the given dataset as the latest dataset for the combination of dataType, company and reporting period
     * Ensures that only one dataset per group has the active status
     */
    @Transactional
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
    @Transactional
    fun setCurrentlyActiveDatasetInactive(
        company: StoredCompanyEntity,
        dataType: String,
        reportingPeriod: String,
    ) {
        val metaInfoOfCurrentlyActiveDataset =
            dataMetaInformationRepository.getActiveDataset(company, dataType, reportingPeriod)
        if (metaInfoOfCurrentlyActiveDataset != null) {
            metaInfoOfCurrentlyActiveDataset.currentlyActive = null
            dataMetaInformationRepository.saveAndFlush(metaInfoOfCurrentlyActiveDataset)
        }
    }

    /**
     * Method to make the data manager get meta info about one specific dataset
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    @Transactional(readOnly = true)
    fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity =
        dataMetaInformationRepository.findById(dataId).orElseThrow {
            ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
            )
        }

    /**
     * Method to make the data manager search for meta info
     * @param searchFilter contains the filters to be applied for the search
     * @return a list of meta info about data depending on the filters
     */
    @Transactional(readOnly = true)
    fun searchDataMetaInfo(searchFilter: DataMetaInformationSearchFilter): List<DataMetaInformationEntity> {
        searchFilter.companyId?.takeIf { it.isNotBlank() }?.let { companyQueryManager.assertCompanyIdExists(it) }
        return dataMetaInformationRepository.searchDataMetaInformation(searchFilter)
    }

    /**
     * Method to delete the data meta information for a given dataId
     * @param dataId of the dataset that should be deleted
     */
    @Transactional
    fun deleteDataMetaInfo(dataId: String) {
        val dataMetaInformation = getDataMetaInformationByDataId(dataId)
        dataMetaInformationRepository.delete(dataMetaInformation)
    }

    /**
     * Queries the meta information for datasets uploaded by a specific user
     * @param userId the id of the user for whom to query data meta information
     * @returns the data meta information uploaded by the specified user
     */
    @Transactional(readOnly = true)
    fun getUserDataMetaInformation(userId: String): List<DataMetaInformationForMyDatasets>? =
        dataMetaInformationRepository
            .getUserUploadsDataMetaInfos(userId)
            .map { DataMetaInformationForMyDatasets.fromDatasetMetaInfoEntityForMyDatasets(it) }

    /**
     * Method to retrieve the latest available dataset meta information for a certain data type and a collection of companies
     * @param companyIds the ids of the companies
     * @param dataType the type of dataset
     * @return the latest available dataset meta information, or null if no dataset is found
     */
    @Transactional(readOnly = true)
    fun getLatestAvailableDatasetMetaInformation(
        companyIds: Collection<String>,
        dataType: String,
    ): List<DataMetaInformationEntity> =
        dataMetaInformationRepository.findLatestActiveByCompanyIdsAndDataType(companyIds, dataType)

    /**
     * Retrieves active dataset metadata for the given exact list of dataset dimensions.
     *
     * @param dataDimensions the dataset dimensions to look up
     * @return list of matching active DataMetaInformationEntity objects
     */
    @Transactional(readOnly = true)
    fun getActiveDataMetaInformationList(dataDimensions: List<BasicDatasetDimensions>): List<DataMetaInformationEntity> {
        if (dataDimensions.isEmpty()) return emptyList()
        val jsonPayload =
            defaultObjectMapper.writeValueAsString(
                dataDimensions.map {
                    mapOf(
                        "company_id" to it.companyId,
                        "framework" to it.framework,
                        "reporting_period" to it.reportingPeriod,
                    )
                },
            )
        return dataMetaInformationRepository.findActiveDatasetsByDimensionsJson(jsonPayload)
    }

    /**
     * Retrieves active dataset metadata matching the given filter criteria.
     * An empty list for any parameter means "match all" (wildcard).
     *
     * @param dataDimensionQuery [DataDimensionQuery] object for which metadata should be resolved
     * @return [List] of [DataMetaInformationEntity] for active datasets matching the filters
     */
    @Transactional(readOnly = true)
    fun getActiveDataMetaInformationList(dataDimensionQuery: DataDimensionQuery): List<DataMetaInformationEntity> =
        if (dataDimensionQuery.isEmpty()) {
            emptyList()
        } else {
            dataMetaInformationRepository
                .findActiveDatasetDimensionsByFilter(
                    defaultObjectMapper.writeValueAsString(dataDimensionQuery.companyIds),
                    defaultObjectMapper.writeValueAsString(dataDimensionQuery.dataTypes),
                    defaultObjectMapper.writeValueAsString(dataDimensionQuery.reportingPeriods),
                )
        }
}
