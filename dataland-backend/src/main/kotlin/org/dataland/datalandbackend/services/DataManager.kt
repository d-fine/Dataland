package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackend.exceptions.InvalidInputApiException
import org.dataland.datalandbackend.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.transaction.Transactional

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DataManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManagerInterface,
    @Autowired var metaDataManager: DataMetaInformationManagerInterface
) : DataManagerInterface {
    private fun getDataMetaInformationByIdAndVerifyDataType(
        dataId: String,
        dataType: DataType
    ): DataMetaInformationEntity {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        if (DataType.valueOf(dataMetaInformation.dataType) != dataType) {
            throw InvalidInputApiException(
                "Requested data $dataId not of type $dataType",
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaInformation.dataType} by Dataland instead of your requested" +
                    " type $dataType."
            )
        }
        return dataMetaInformation
    }

    @Transactional
    override fun addDataSet(storableDataSet: StorableDataSet): String {
        val company = companyManager.getCompanyById(storableDataSet.companyId)
        val dataId = edcClient.insertData(objectMapper.writeValueAsString(storableDataSet)).dataId
        metaDataManager.storeDataMetaInformation(company, dataId, storableDataSet.dataType)
        return dataId
    }

    override fun getDataSet(dataId: String, dataType: DataType): StorableDataSet {
        getDataMetaInformationByIdAndVerifyDataType(dataId, dataType)
        val dataAsString = edcClient.selectDataById(dataId)
        if (dataAsString == "") {
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store."
            )
        }
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        if (dataAsStorableDataSet.dataType != dataType) {
            throw InternalServerErrorApiException(
                "Dataland-Internal inconsistency regarding dataset $dataId",
                "We are having some internal issues with the dataset $dataId, please contact support.",
                "Dataset $dataId should be of type $dataType but is of type ${dataAsStorableDataSet.dataType}"
            )
        }
        return dataAsStorableDataSet
    }

    override fun isDataSetPublic(dataId: String): Boolean {
        val associatedCompanyId = metaDataManager.getDataMetaInformationByDataId(dataId).company.companyId
        return companyManager.isCompanyPublic(associatedCompanyId)
    }
}
