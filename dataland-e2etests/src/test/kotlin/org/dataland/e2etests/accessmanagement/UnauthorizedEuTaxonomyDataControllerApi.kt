package org.dataland.e2etests.accessmanagement

import com.squareup.moshi.JsonAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY

class UnauthorizedEuTaxonomyDataControllerApi {

    private val client = OkHttpClient()

    private fun transferJsonToCompanyAssociatedDataEuTaxonomyData(inputString: String):
        CompanyAssociatedDataEuTaxonomyData {
        val jsonAdapter: JsonAdapter<CompanyAssociatedDataEuTaxonomyData> =
            moshi.adapter(CompanyAssociatedDataEuTaxonomyData::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun buildGetCompanyAssociatedDataEuTaxonomyDataRequest(dataId: String): Request {
        return Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_PROXY/data/eutaxonomies/$dataId")
            .get()
            .build()
    }

    fun getCompanyAssociatedDataEuTaxonomyData(dataId: String): CompanyAssociatedDataEuTaxonomyData {
        val response = client.newCall(buildGetCompanyAssociatedDataEuTaxonomyDataRequest(dataId)).execute()
        if (!response.isSuccessful) throw IllegalArgumentException("Unauthorized access failed, response is: $response")
        val responseBodyAsString = response.body!!.string()
        return transferJsonToCompanyAssociatedDataEuTaxonomyData(responseBodyAsString)
    }
}
