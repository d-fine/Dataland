package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDataForNonFinancials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the EU Taxonomy endpoints of non-financial companies
 * @param myDataManager data manager to be used
 * @param myObjectMapper object mapper used for converting data classes to strings and vice versa
 */
@RequestMapping("/data/eutaxonomy-non-financials")
@RestController
class EuTaxonomyDataForNonFinancialsController(
    @Autowired var myDataManager: DataManagerInterface,
    @Autowired var myMetaDataManager: DataMetaInformationManagerInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyDataForNonFinancials>(
    myDataManager,
    myMetaDataManager,
    myObjectMapper,
    EuTaxonomyDataForNonFinancials::class.java
)
