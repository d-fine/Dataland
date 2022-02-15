package org.dataland.datalandbackend.interfaces

import org.dataland.datalandbackend.model.DataSet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DataStoreInterfaceTest : DataStoreInterface {
    val dataSets = listOf(
        DataSet(name = "Company A", payload = "Data"),
        DataSet(name = "Holding B", payload = "Information"),
        DataSet(name = "Group C", payload = "Inputs")
    )

    @Test
    fun `Add a dataset, provoke an error`() {
        assertThrows<NotImplementedError> {
            addDataSet(dataSets[0])
        }
    }

    @Test
    fun `List the datasets, provoke an error`() {
        assertThrows<NotImplementedError> {
            listDataSets()
        }
    }

    @Test
    fun `Get a dataset, provoke an error`() {
        assertThrows<NotImplementedError> {
            getDataSet(dataSets[0].name)
        }
    }
}
