package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A service class for managing data meta-information
 */
@Component("DataMetaInformationManager")
class DataMetaInformationManager(
    @Autowired private val dataMetaInformationRepository: DataMetaInformationRepository,
    @Autowired private val companyManager: CompanyManagerInterface
) : DataMetaInformationManagerInterface {
    override fun storeDataMetaInformation(
        company: StoredCompanyEntity,
        dataId: String,
        dataType: DataType
    ): DataMetaInformationEntity {
        val dataMetaInformationEntity = DataMetaInformationEntity(
            dataId = dataId,
            dataType = dataType.name,
            company = company,
        )

        return dataMetaInformationRepository.save(dataMetaInformationEntity)
    }

    override fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity {
        val dataMetaInformationDbResponse = dataMetaInformationRepository.findById(dataId)
        if (dataMetaInformationDbResponse.isEmpty) {
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store."
            )
        }
        return dataMetaInformationDbResponse.get()
    }

    override fun searchDataMetaInfo(companyId: String, dataType: DataType?): List<DataMetaInformationEntity> {
        if (companyId != "")
            companyManager.verifyCompanyIdExists(companyId)
        val dataTypeFilter = dataType?.name ?: ""
        return dataMetaInformationRepository.searchDataMetaInformation(
            DataMetaInformationSearchFilter(
                dataTypeFilter = dataTypeFilter,
                companyIdFilter = companyId
            )
        )
    }
}
