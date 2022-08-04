package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.EuTaxonomyDataForFinancials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints of financial companies
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/eutaxonomy/financials")
@RestController
class EuTaxonomyDataForFinancialsController(
    @Autowired var myDataManager: DataManagerInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyDataForFinancials>(
    myDataManager,
    myObjectMapper,
    EuTaxonomyDataForFinancials::class.java
)
