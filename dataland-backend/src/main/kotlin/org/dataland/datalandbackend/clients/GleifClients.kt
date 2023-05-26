package org.dataland.datalandbackend.clients

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.dataland.datalandbackend.model.CompanyIdentifier
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.json.JSONObject

class GleifClients {
    private val client = OkHttpClient.Builder().connectTimeout(5000, TimeUnit.MILLISECONDS).build()

    private fun mapGleifResponseToCompanyInformation(responseBody: String): CompanyInformation {
        println("responseBody: $responseBody")
        val attributes = JSONObject(responseBody).getJSONObject("data").getJSONObject("attributes")
        val lei = attributes.getString("lei")
        val companyName = attributes.getString("entity.legalname.name")
        val countryCode = attributes.getString("entity.legaladdress.country")
        val headquarters = attributes.getString("entity.legaladdress.city")
        val headquartersPostalCode = attributes.getString("entity.legaladdress.postalcode")

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
        val mediaType = "text/plain".toMediaTypeOrNull()
        val requestBody = "".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.gleif.org/api/v1/lei-records/$lei")
            .method("GET", requestBody)
            .addHeader("Accept", "application/vnd.api+json")
            .build()
        val response = client.newCall(request).execute()
        println("response: $response")
        return mapGleifResponseToCompanyInformation(response.body!!.string())
    }
}