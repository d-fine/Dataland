package org.dataland.datalandbackend.clients

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.json.JSONObject

class GleifClients {
    private val client = OkHttpClient.Builder().connectTimeout(5000, TimeUnit.MILLISECONDS).build()

    private fun mapGleifResponseToCompanyInformation(responseBody: String): CompanyInformation {
        println("responseBody: $responseBody")
        val attributes = JSONObject(responseBody).getJSONObject("data").getJSONObject("attributes")
        println("attributes: $attributes")
        val lei = attributes.getString("lei")
        println("lei: $lei")
        val entity = attributes.getJSONObject("entity")
        println("entity: $entity")
        val address = entity.getJSONObject("headquartersAddress")
        println("address: $address")
        val name = entity.getJSONObject("legalName")
        println("name: $name")
        val companyName = name.getString("name")
        val countryCode = address.getString("country")
        val headquarters = address.getString("city")
        val headquartersPostalCode = address.getString("postalCode")

        return CompanyInformation(
            companyName = companyName,
            companyAlternativeNames = null,
            //TODO legalForm.id existiert kann man damit etwas anfangen?
            companyLegalForm = null,
            countryCode = countryCode,
            headquarters = headquarters,
            headquartersPostalCode = headquartersPostalCode,
            sector = "dummy",
            website = null,
            identifiers = listOf(CompanyIdentifier(identifierType = IdentifierType.Lei, identifierValue = lei))
        )
    }

    fun getCompanyInformationByLei(lei: String): CompanyInformation {
        val request = Request.Builder()
            .url("https://api.gleif.org/api/v1/lei-records/$lei")
            .method("GET", null)
            .addHeader("Accept", "application/vnd.api+json")
            .build()
        val response = client.newCall(request).execute()
        println("response: $response")
        return mapGleifResponseToCompanyInformation(response.body!!.string())
    }
}