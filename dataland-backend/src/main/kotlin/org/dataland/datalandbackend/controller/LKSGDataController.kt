package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.lksg.LKSGData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the LKSG framework endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/lksg")
@RestController
class LKSGDataController(
    @Autowired var myDataManager: DataManagerInterface,
    @Autowired var myMetaDataManager: DataMetaInformationManagerInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<LKSGData>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    LKSGData::class.java
)
