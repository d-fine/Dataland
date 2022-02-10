package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InMemoryDataStoreTest {

    @Test
    fun check_name_after_adding_new_data() {
        val testStore = InMemoryDataStore()
        val identifier = testStore.addDataSet(dataSet = dataSets[0])
        assertEquals(identifier.name, dataSets[0].name)
    }

    @Test
    fun check_id_after_adding_multiple_new_data() {
        val testStore = InMemoryDataStore()
        var identifier = Identifier(name = "dummy", id = "0")
        for (dataSet in dataSets) {
            identifier = testStore.addDataSet(dataSet = dataSet)
        }
        assertEquals(identifier.id, (dataSets.size - 1).toString())
    }

    companion object {
        val dataSets = listOf<DataSet>(
            DataSet(name = "Company A", payload = "Data"),
            DataSet(name = "Holding B", payload = "Information"),
            DataSet(name = "Group C", payload = "Inputs")
        )
    }
}
