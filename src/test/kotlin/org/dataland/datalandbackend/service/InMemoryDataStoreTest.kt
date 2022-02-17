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
    fun `Check that the name of the added data is as expected`() {
        val testStore = InMemoryDataStore()
        val identifier = testStore.addDataSet(dataSet = dataSets[0])
        assertEquals(identifier.name, dataSets[0].name)
    }

    @Test
    fun `Add and retrieve data as a list and check dataset`() {
        for (dataset in dataSets)
            testStore.addDataSet(dataSet = dataset)
        assertEquals(dataSets[0].name, testStore.listDataSets()[0].name)
    }

    @Test
    fun `Add and retrieve data`() {
        testStore.addDataSet(dataSet = dataSets[1])
        assertEquals(dataSets[1].name, testStore.listDataSets()[0].name)
    }

    @Test
    fun `Get dataset with id that does not exist`() {
        assertThrows<IllegalArgumentException> {
            testStore.getDataSet("error")
        }
    }

    @Test
    fun `Get the dataset by id`() {
        testStore.addDataSet(dataSet = dataSets[1])
        assertEquals(dataSets[1], testStore.getDataSet("0"))
    }

    @Test
    fun `Get dataset message`() {
        val id = "2"
        val expectedMessage = "The id: $id does not exist."
        val exceptionThatWasThrown: Throwable = assertThrows<IllegalArgumentException> {
            testStore.getDataSet(id)
        }
        assertEquals(expectedMessage, exceptionThatWasThrown.message)
    }

    @Test
    fun `Check that the id is correct after adding multiple data sets`() {
        val testStore = InMemoryDataStore()
        var dataSetMetaInformation: DataSetMetaInformation? = null
        for (dataSet in dataSets) {
            dataSetMetaInformation = testStore.addDataSet(dataSet = dataSet)
        }
        assertEquals(dataSetMetaInformation!!.id, dataSets.size.toString())
    }
}
