package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.EuTaxonomyDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/EU-Taxonomy")
@RestController
class EuTaxonomyDataController(
    @Autowired @Qualifier("DefaultStore") var myDataStore: DataStoreInterface,
    @Autowired var myObjectMapper: ObjectMapper
) : DataController<EuTaxonomyDataSet>(myDataStore, myObjectMapper) {
    override fun getClazz(): Class<EuTaxonomyDataSet> {
        return EuTaxonomyDataSet::class.java
    }
}
