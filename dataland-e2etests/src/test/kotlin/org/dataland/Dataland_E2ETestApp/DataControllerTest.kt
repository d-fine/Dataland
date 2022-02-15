package org.dataland.Dataland_E2ETestApp

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.dataland.Dataland_E2ETestApp.clients.backend.apis.HealthControllerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.openapitools.client.apis.DataControllerApi
import org.openapitools.client.models.DataSet


class DataControllerTest {

    @Test
    fun testPostAndGetData() {
        // Before:
        val dataControllerApi = DataControllerApi(basePath = "http://backend:8080")
        val dataSetName = "TestName_007"
        val payload = "testDataSet_007"

        //Get data and count entries:
        var allData = dataControllerApi.getData()
        val numberOfEntriesBefore = allData.size

        //Add data:
        val dataSet = DataSet(name=dataSetName, payload=payload)
        val postResponse = dataControllerApi.postData(dataSet)

        //Get data and count entries again:
        allData = dataControllerApi.getData()
        val numberOfEntriesAfter = allData.size

        // Test if number of entries increased by one data set:
        Assertions.assertEquals(numberOfEntriesAfter, numberOfEntriesBefore+1)
        // Test if actual data set was posted:
        Assertions.assertEquals(dataSetName, postResponse.name)

        //To-Do: Clean-up to delete inserted data?

    }
}