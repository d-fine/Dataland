package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataProcessorInterface
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Implementation of a data processor for Dataland including metadata storages
 */
@Component
class DataProcessor(@Autowired var edcClient: DefaultApi) : DataProcessorInterface, DataManager() {
    override fun searchDataMetaInfo(dataId: String, companyId: String, dataType: String): List<DataMetaInformation> {
        if (dataId.isNotEmpty()) {
            verifyDataIdExists(dataId)
            return listOf(dataMetaData[dataId]!!)
        }

        var matches: Map<String, DataMetaInformation> = dataMetaData

        if (companyId.isNotEmpty()) {
            verifyCompanyIdExists(companyId)
            matches = matches.filter { it.value.companyId == companyId }
        }
        if (dataType.isNotEmpty()) {
            verifyDataTypeExists(dataType)
            matches = matches.filter { it.value.dataType == dataType }
        }

        return matches.map {
            DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId)
        }
    }

    override fun addDataSet(storableDataSet: StorableDataSet): String {
        verifyCompanyIdExists(storableDataSet.companyId)

        val dataId = edcClient.insertData(storableDataSet.data)

        if (dataMetaData.containsKey(dataId)) {
            throw IllegalArgumentException("The data ID $dataId already exists in Dataland.")
        }

        val dataMetaInformation =
            DataMetaInformation(dataId, dataType = storableDataSet.dataType, companyId = storableDataSet.companyId)
        dataMetaData[dataId] = dataMetaInformation
        companyData[storableDataSet.companyId]!!.dataRegisteredByDataland.add(dataMetaInformation)
        return dataId
    }

    override fun getData(dataManagerInputToGetData: DataManagerInputToGetData): String {
        verifyDataIdExistsAndIsOfType(dataManagerInputToGetData.dataId, dataManagerInputToGetData.dataType)

        val data = edcClient.selectDataById(dataManagerInputToGetData.dataId)

        if (data == "") {
            throw IllegalArgumentException(
                "No data set with the id: ${dataManagerInputToGetData.dataId} " +
                    "could be found in the data store."
            )
        }
        return data
    }
}
