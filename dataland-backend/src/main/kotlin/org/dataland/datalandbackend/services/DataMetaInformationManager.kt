package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component("DataMetaInformationManager")
class DataMetaInformationManager(
    @Autowired private val dataMetaInformationRepository: DataMetaInformationRepository,
) : DataMetaInformationManagerInterface {
    override fun storeDataMetaInformation(company: StoredCompanyEntity, dataId: String, dataType: DataType): DataMetaInformationEntity {
        val dataMetaInformationEntity = DataMetaInformationEntity(
            dataId = dataId,
            dataType = dataType.name,
            company = company,
        )

        val savedDataMetaInformationEntity = dataMetaInformationRepository.save(dataMetaInformationEntity)
        return savedDataMetaInformationEntity
    }

    override fun getDataMetaInformationByDataId(dataId: String): DataMetaInformationEntity {
        val dataMetaInformationDbResponse = dataMetaInformationRepository.findById(dataId)
        if (dataMetaInformationDbResponse.isEmpty) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId")
        }
        return dataMetaInformationDbResponse.get()
    }

    override fun searchDataMetaInfo(companyId: String, dataType: DataType?): List<DataMetaInformationEntity> {
        return if (dataType === null) {
            dataMetaInformationRepository.getByCompanyCompanyId(companyId)
        } else {
            dataMetaInformationRepository.getByCompanyCompanyIdAndDataType(companyId, dataType.name)
        }
    }
}