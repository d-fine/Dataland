package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including metadata storages
 */
@Component("DefaultManager")
class DataManager {
    var dataMetaData = mutableMapOf<String, DataMetaInformation>()
    var companyData = mutableMapOf<String, CompanyMetaInformation>()
    internal var companyCounter = 0

    internal fun verifyCompanyIdExists(companyId: String) {
        if (!companyData.containsKey(companyId)) {
            throw IllegalArgumentException("Dataland does not know the company ID $companyId.")
        }
    }

    internal fun verifyDataIdExists(dataId: String) {
        if (!dataMetaData.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId.")
        }
    }

    internal fun verifyDataTypeExists(dataType: String) {
        val matchesForDataType = dataMetaData.any { it.value.dataType == dataType }
        if (!matchesForDataType) {
            throw IllegalArgumentException("Dataland does not know the data type: $dataType")
        }
    }

    internal fun verifyDataIdExistsAndIsOfType(dataId: String, dataType: String) {
        verifyDataIdExists(dataId)
        if (dataMetaData[dataId]!!.dataType != dataType) {
            throw IllegalArgumentException(
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaData[dataId]!!.dataType} by Dataland instead of your requested" +
                    " type $dataType."
            )
        }
    }
}
