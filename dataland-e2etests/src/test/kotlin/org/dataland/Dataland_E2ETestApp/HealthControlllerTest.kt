package org.dataland.Dataland_E2ETestApp.tests

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.dataland.Dataland_E2ETestApp.clients.backend.apis.HealthControllerApi
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.openapitools.client.apis.DataControllerApi


class HealthControlllerTest {

    @Test
    fun testGetHealth() {
        val responseMessage =
        When {
            get("http://backend:8080/actuator/health")
        } Then {
            statusCode(200)
        } Extract {
            response().body.asString()
        }
        Assertions.assertEquals("UP", responseMessage)
    }

    // Before:
/*
    @Test
    fun testGetHealthWithClientCode() {
        // Before:
        val healthControllerApi = HealthControllerApi(basePath = "http://localhost:8080")
        val responseMessage = healthControllerApi.getHealth()
        // print(responseMessage)
        // Test:
        // Assertions.assertEquals("Healthy", responseMessage)
    }
*/
}
