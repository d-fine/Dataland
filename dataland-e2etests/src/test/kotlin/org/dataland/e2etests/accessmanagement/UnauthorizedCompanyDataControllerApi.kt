package org.dataland.e2etests.accessmanagement

import com.squareup.moshi.JsonAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND

class UnauthorizedCompanyDataControllerApi {

    private val client = OkHttpClient()

    private fun transferJsonToStoredCompany(inputString: String): StoredCompany {
        val jsonAdapter: JsonAdapter<StoredCompany> = moshi.adapter(StoredCompany::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun buildGetCompanyByIdRequest(companyId: String): Request {
        return Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/public/companies/$companyId")
            .get()
            .build()
    }

    fun getCompanyById(companyId: String): StoredCompany {
        val response = client.newCall(buildGetCompanyByIdRequest(companyId)).execute()
        if (!response.isSuccessful) throw IllegalArgumentException("Unauthorized access failed, response is: $response")
        val responseBodyAsString = response.body.string()
        return transferJsonToStoredCompany(responseBodyAsString)
    }
}
