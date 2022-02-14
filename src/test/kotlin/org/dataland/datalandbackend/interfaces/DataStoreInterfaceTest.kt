package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataSet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DataStoreInterfaceTest : DataStoreInterface {

    @Test
    fun addDataSetNotImplemented() {
        assertThrows<NotImplementedError> {
            addDataSet(dataSets[0])
        }
    }

    @Test
    fun listDataSetsNotImplemented() {
        assertThrows<NotImplementedError> {
            listDataSets()
        }
    }

    @Test
    fun getDataSet() {
        assertThrows<NotImplementedError> {
            getDataSet(dataSets[0].name)
        }
    }

    companion object {
        val dataSets = listOf<DataSet>(
            DataSet(name = "Company A", payload = "Data"),
            DataSet(name = "Holding B", payload = "Information"),
            DataSet(name = "Group C", payload = "Inputs")
        )
    }
}
