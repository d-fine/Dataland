package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * A service class for managing data meta-information
 */
@Component("DataMetaInformationManager")
class DataMetaInformationManager(
    @Autowired private val dataMetaInformationRepository: DataMetaInformationRepository,
    @Autowired private val companyManager: CompanyManager,
) {

    /**
     * Method to associate data information with a specific company
     * @param company The company to associate the data meta information with
     * @param dataId The id of the dataset to associate with the company
     * @param dataType The dataType of the dataId
     */
    @Transactional
    fun storeDataMetaInformation(
        dataId: String,
        dataType: DataType,
        uploaderUserId: String,
        uploadTime: Long,
        company: StoredCompanyEntity,
    ): DataMetaInformationEntity {
        val dataMetaInformationEntity = DataMetaInformationEntity(
            dataId = dataId,
            dataType = dataType.name,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
            company = company,
        )

        return dataMetaInformationRepository.save(dataMetaInformationEntity)
    }

    /**
     * Method to make the data manager get meta info about one specific data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return meta info about data behind the dataId
     */
    fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity {
        val dataMetaInformationDbResponse = dataMetaInformationRepository.findById(dataId)
        if (dataMetaInformationDbResponse.isEmpty) {
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
            )
        }
        return dataMetaInformationDbResponse.get()
    }

    /**
     * Method to make the data manager search for meta info
     * @param companyId if not empty, it filters the requested meta info to a specific company
     * @param dataType if not empty, it filters the requested meta info to a specific data type
     * @return a list of meta info about data depending on the filters:
     */
    fun searchDataMetaInfo(companyId: String, dataType: DataType?): List<DataMetaInformationEntity> {
        if (companyId != "") {
            companyManager.verifyCompanyIdExists(companyId)
        }
        val dataTypeFilter = dataType?.name ?: ""
        return dataMetaInformationRepository.searchDataMetaInformation(
            DataMetaInformationSearchFilter(
                dataTypeFilter = dataTypeFilter,
                companyIdFilter = companyId,
            ),
        )
    }
}
