package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.AllDataAPI
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataMetaInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the API for company data exchange
 * @param dataManager implementation of the DataManagerInterface that defines how
 * Dataland handles data
 */

@RestController
class AllDataController(
    @Autowired var dataManager: DataManagerInterface,
) : AllDataAPI {

    override fun getListOfDataMetaInfo(companyId: String?, dataType: String?): ResponseEntity<List<DataMetaInformation>> {
        return ResponseEntity.ok(this.dataManager.searchDataMetaInfo(companyId = companyId ?: "", dataType = dataType ?: ""))
    }

    override fun getDataMetaInfo(dataId: String): ResponseEntity<DataMetaInformation> {
        return ResponseEntity.ok(this.dataManager.searchDataMetaInfo(dataId = dataId).elementAt(0))
    }
}

/*
 /**
     * A method to search for meta info about data sets registered by Dataland
     * @param companyId filters the requested meta info to a specific company.
     * @param dataType filters the requested meta info to a specific data type.
     * @return a list of matching DataMetaInformation
     */
    fun getListOfDataMetaInfo(@RequestParam companyId: String? = null, @RequestParam dataType: String? = null):
        List<DataMetaInformation>


    /**
     * A method to retrieve meta info about a specific data set
     * @param dataId as unique identifier for a specific data set
     * @return the DataMetaInformation for the specified data set
     */
    fun getDataMetaInfo(@PathVariable dataId: String): DataMetaInformation
 */