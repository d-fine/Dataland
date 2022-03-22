package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints
 * @param myDataStore implementation of the data store to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/eutaxonomies")
@RestController
class EuTaxonomyDataController(
    @Autowired var myDataStore: DataStoreInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyData>(myDataStore, myObjectMapper, EuTaxonomyData::class.java)
