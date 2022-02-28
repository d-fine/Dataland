package org.dataland.e2etests

import org.dataland.datalandbackend.client.api.DataControllerApi
import org.dataland.datalandbackend.client.model.DataSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataControllerTest {
    val dataControllerApi = DataControllerApi(basePath = "http://backend:8080")

    @Test
    fun `post a dummy data set and check if post was successful`() {
        val testDataSetName = "TestName_007"
        val testDataSetPayload = "testDataSet_007"
        val testDataSet = DataSet(name = testDataSetName, payload = testDataSetPayload)

        var allData = dataControllerApi.getData()
        val numberOfEntriesBeforePost = allData.size

        val postResponse = dataControllerApi.postData(testDataSet)

        allData = dataControllerApi.getData()
        val numberOfEntriesAfterPost = allData.size

        Assertions.assertEquals(
            numberOfEntriesAfterPost,
            numberOfEntriesBeforePost + 1,
            "Number of entries did not increase by exactly one data set."
        )
        Assertions.assertEquals(testDataSetName, postResponse.name, "The actual test data set was not posted.")
    }

    @Test
    fun `post a dummy data set and check if that specific data set can be queried by its ID`() {
        val testDataSetName = "TestName_008"
        val testDataSetPayload = "testDataSet_008"
        val testDataSet = DataSet(name = testDataSetName, payload = testDataSetPayload)

        val postResponse = dataControllerApi.postData(testDataSet)
        val testDataSetID = postResponse.id

        val getResponse = dataControllerApi.getDataSet(testDataSetID)

        Assertions.assertEquals(
            testDataSetName,
            getResponse.name,
            "The 'name' value of the data set in the getResponse does not match the " +
                "'name' value of the test data set that was posted before."
        )
        Assertions.assertEquals(
            testDataSetPayload,
            getResponse.payload,
            "The 'payload' value of the data set in the getResponse does not match the " +
                "'payload' value of the test data set that was posted before."
        )
    }

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        // ...
    }
}
