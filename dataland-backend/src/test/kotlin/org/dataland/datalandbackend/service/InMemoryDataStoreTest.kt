package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.model.DataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InMemoryDataStoreTest {
    val testStore = InMemoryDataStore()
    val dataSets = listOf(
        DataSet(name = "Company A", payload = "Data"),
        DataSet(name = "Holding B", payload = "Information"),
        DataSet(name = "Group C", payload = "Inputs")
    )

    @Test
    fun `add the first dataset and check if the name is as expected by using the return value of addDataSet`() {
        val identifier = testStore.addDataSet(dataSet = dataSets[0])
        assertEquals(identifier.name, dataSets[0].name)
    }

    @Test
    fun `add all datasets and retrieve them as a list and check for each dataset if the name is as expected`() {
        for (dataset in dataSets) {
            testStore.addDataSet(dataSet = dataset)
        }

        val allDataSetsInStore = testStore.listDataSets()

        for ((counter, storedDataSet) in allDataSetsInStore.withIndex()) {
            assertEquals(dataSets[counter].name, storedDataSet.name)
        }
    }

    @Test
    fun `get dataset with id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testStore.getDataSet("error")
        }
    }

    @Test
    fun `add and get dataset by id`() {
        val identifier = testStore.addDataSet(dataSet = dataSets[1])
        assertEquals(dataSets[1], testStore.getDataSet(identifier.id))
    }

    @Test
    fun `get dataset error message`() {
        val id = "2"
        val expectedMessage = "The id: $id does not exist."
        val exceptionThatWasThrown: Throwable = assertThrows<IllegalArgumentException> {
            testStore.getDataSet(id)
        }
        assertEquals(expectedMessage, exceptionThatWasThrown.message)
    }

    @Test
    fun `check if the id of the last dataset equals the total number of all datasets after adding them all`() {
        var dataSetMetaInformation: DataSetMetaInformation? = null
        for (dataSet in dataSets) {
            dataSetMetaInformation = testStore.addDataSet(dataSet = dataSet)
        }
        assertEquals(dataSetMetaInformation!!.id, dataSets.size.toString())
    }
}
