package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.DataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataControllerTest {
    val dataControllerApi = DataControllerApi(basePath = "http://backend:8080/api")

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

        assertEquals(
            numberOfEntriesAfterPost,
            numberOfEntriesBeforePost + 1,
            "Number of entries did not increase by exactly one data set."
        )
        assertEquals(testDataSetName, postResponse.name, "The actual test data set was not posted.")
    }

    @Test
    fun `post a dummy data set and check if that specific data set can be queried by its ID`() {
        val testDataSetName = "TestName_008"
        val testDataSetPayload = "testDataSet_008"
        val testDataSet = DataSet(name = testDataSetName, payload = testDataSetPayload)

        val postResponse = dataControllerApi.postData(testDataSet)
        val testDataSetID = postResponse.id

        val getResponse = dataControllerApi.getDataSet(testDataSetID)

        assertEquals(
            testDataSetName,
            getResponse.name,
            "Response had name: ${getResponse.name} which does not match the posted name: $testDataSetName."
        )
        assertEquals(
            testDataSetPayload,
            getResponse.payload,
            "Response had payload: ${getResponse.payload} which does not match the posted name: $testDataSetPayload."
        )
    }

    @Test
    fun `get dummy company data by sending a request to dummy skyminder server`() {
        assert(dataControllerApi.getDataSkyminderRequest(code = "dummy", name = "dummy").isNotEmpty())
    }
}
