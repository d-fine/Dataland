package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.EuTaxonomyDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
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
    @Autowired @Qualifier("DefaultManager") var myDataManager: DataManagerInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyDataSet>(myDataManager, myObjectMapper) {
    override fun getClazz(): Class<EuTaxonomyDataSet> {
        return EuTaxonomyDataSet::class.java
    }
}
