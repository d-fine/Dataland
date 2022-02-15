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

   /* @Test
    fun check_constructor_data() {
        assertThat(testStore.data, instanceOf(MutableMap::class.java))
    }*/

    @Test
    fun check_name_after_adding_new_data() {
        val identifier = testStore.addDataSet(dataSet = dataSets[0])
        assertEquals(identifier.name, dataSets[0].name)
    }

    @Test
    fun check_id_after_adding_multiple_new_data() {
        var identifier = DataSetMetaInformation(name = "dummy", id = "0")
        for (dataSet in dataSets) {
            identifier = testStore.addDataSet(dataSet = dataSet)
        }
        assertEquals(identifier.id, (dataSets.size - 1).toString())
    }

    @Test
    fun add_and_retrieve_data_as_list_0() {
        for (dataset in dataSets)
            testStore.addDataSet(dataSet = dataset)
        assertEquals(dataSets[0].name, testStore.listDataSets()[0].name)
    }

    @Test
    fun add_and_retrieve_data_as_list_1() {
        for (dataset in dataSets)
            testStore.addDataSet(dataSet = dataset)
        assertEquals(dataSets[1].name, testStore.listDataSets()[1].name)
    }

    @Test
    fun add_and_retrieve_data_as_list_2() {
        for (dataset in dataSets)
            testStore.addDataSet(dataSet = dataset)
        assertEquals(dataSets[2].name, testStore.listDataSets()[2].name)
    }

    @Test
    fun add_and_retrieve_data() {
        testStore.addDataSet(dataSet = dataSets[1])
        assertEquals(dataSets[1].name, testStore.listDataSets()[0].name)
    }

    @Test
    fun get_dataset_id_not_existing() {
        assertThrows<IllegalArgumentException> {
            testStore.getDataSet("error")
        }
    }

    @Test
    fun get_dataset_id_exists() {
        testStore.addDataSet(dataSet = dataSets[1])
        println(testStore.listDataSets())
        assertEquals(dataSets[1], testStore.getDataSet("0")) // why having starting id == 0 not 1?
    }

    @Test
    fun get_dataset_message() {
        val id = "2"
        val expectedMessage = "The id: $id does not exist."
        val exceptionThatWasThrown: Throwable = assertThrows<IllegalArgumentException> {
            testStore.getDataSet(id)
        }
        assertEquals(expectedMessage, exceptionThatWasThrown.message)
    }

    companion object {
        val dataSets = listOf(
            DataSet(name = "Company A", payload = "Data"),
            DataSet(name = "Holding B", payload = "Information"),
            DataSet(name = "Group C", payload = "Inputs")
        )
    }
}
