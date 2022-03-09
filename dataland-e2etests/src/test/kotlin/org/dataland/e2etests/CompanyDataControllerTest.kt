package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.DataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CompanyDataControllerTest {
    val dataControllerApi = DataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `TO-DO TO-DO TO-Do`() {     // TODO
        assertEquals(1, 1)
    }
}


/*  TEMPLATE
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

 */