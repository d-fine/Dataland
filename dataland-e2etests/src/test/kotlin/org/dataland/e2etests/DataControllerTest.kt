package org.dataland.e2etests

import org.dataland.datalandbackend.client.api.DataControllerApi
import org.dataland.datalandbackend.client.model.DataSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataControllerTest {
    val dataControllerApi = DataControllerApi(basePath = "http://backend:8080")

    @Test
    fun `Post a dummy data set and check if post was successful`() {
        val dataSetName = "TestName_007"
        val dataSetPayload = "testDataSet_007"
        val dataSet = DataSet(name = dataSetName, payload = dataSetPayload)

        var allData = dataControllerApi.getData()
        val numberOfEntriesBeforePost = allData.size

        val postResponse = dataControllerApi.postData(dataSet)

        allData = dataControllerApi.getData()
        val numberOfEntriesAfterPost = allData.size

        Assertions.assertEquals(numberOfEntriesAfterPost, numberOfEntriesBeforePost + 1, "Number of entries did not increase by exactly one data set.")
        Assertions.assertEquals(dataSetName, postResponse.name, "The actual data set was not posted.")
    }

    @Test
    fun `Post a dummy data set and check if that specific data set can be queried by its ID`() {
        val dataSetName = "TestName_008"
        val dataSetPayload = "testDataSet_008"
        val dataSet = DataSet(name = dataSetName, payload = dataSetPayload)

        val postResponse = dataControllerApi.postData(dataSet)
        val dataSetID = postResponse.id

        val response = dataControllerApi.getDataSet(dataSetID)

        Assertions.assertEquals(dataSetName, response.name, "The 'name' value of the data set in the response does not match the 'name' value of the data set that was posted before.")
        Assertions.assertEquals(dataSetPayload, response.payload, "The 'payload' value of the data set in the response does not match the 'payload' value of the data set that was posted before.")
    }
}
