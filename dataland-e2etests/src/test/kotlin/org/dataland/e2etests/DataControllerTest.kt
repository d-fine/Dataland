package org.dataland.e2etests

import org.dataland.datalandbackend.client.api.DataControllerApi
import org.dataland.datalandbackend.client.model.DataSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataControllerTest {

    @Test
    fun `post a dummy data set and check if total number of datasets increased`() {
        // Before:
        val dataControllerApi = DataControllerApi(basePath = "http://backend:8080")
        val dataSetName = "TestName_007"
        val payload = "testDataSet_007"

        // Get data and count entries:
        var allData = dataControllerApi.getData()
        val numberOfEntriesBefore = allData.size

        // Add data:
        val dataSet = DataSet(name = dataSetName, payload = payload)
        val postResponse = dataControllerApi.postData(dataSet)

        // Get data and count entries again:
        allData = dataControllerApi.getData()
        val numberOfEntriesAfter = allData.size

        // Test if number of entries increased by one data set:
        Assertions.assertEquals(numberOfEntriesAfter, numberOfEntriesBefore + 1)
        // Test if actual data set was posted:
        Assertions.assertEquals(dataSetName, postResponse.name)
        //Todo: remove this line
        Assertions.assertTrue(false)

        // To-Do: Clean-up to delete inserted data?
    }
}
