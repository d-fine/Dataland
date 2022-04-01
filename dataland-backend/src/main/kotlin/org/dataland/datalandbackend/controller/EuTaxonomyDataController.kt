package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataProcessorInterface
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints
 * @param euTaxonomyDataManager data manager to be used
 * @param euTaxonomyObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/eutaxonomies")
@RestController
class EuTaxonomyDataController(
    @Autowired var euTaxonomyDataManager: DataProcessorInterface,
    @Autowired var euTaxonomyObjectMapper: ObjectMapper
) : DataController<EuTaxonomyData>(
    euTaxonomyDataManager,
    euTaxonomyObjectMapper,
    EuTaxonomyData::class.java
)
