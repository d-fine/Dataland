package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/eutaxonomies")
@RestController
class EuTaxonomyDataController(
    @Autowired var myDataManager: DataManagerInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyData>(
    myDataManager,
    myObjectMapper,
    EuTaxonomyData::class.java
)
