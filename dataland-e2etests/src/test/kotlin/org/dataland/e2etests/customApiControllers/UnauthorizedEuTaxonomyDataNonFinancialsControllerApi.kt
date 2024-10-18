package org.dataland.e2etests.customApiControllers

import com.squareup.moshi.JsonAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND

class UnauthorizedEuTaxonomyDataNonFinancialsControllerApi {
    private val client = OkHttpClient()

    private fun transferJsonToCompanyAssociatedDataEuTaxonomyDataForNonFinancials(
        inputString: String,
    ): CompanyAssociatedDataEutaxonomyNonFinancialsData {
        val jsonAdapter: JsonAdapter<CompanyAssociatedDataEutaxonomyNonFinancialsData> =
            moshi.adapter(CompanyAssociatedDataEutaxonomyNonFinancialsData::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun buildGetCompanyAssociatedDataEuTaxonomyDataForNonFinancialsRequest(dataId: String): Request =
        Request
            .Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/eutaxonomy-non-financials/$dataId")
            .get()
            .build()

    fun getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(dataId: String): CompanyAssociatedDataEutaxonomyNonFinancialsData {
        val response =
            client
                .newCall(buildGetCompanyAssociatedDataEuTaxonomyDataForNonFinancialsRequest(dataId))
                .execute()
        require(response.isSuccessful) { "Unauthorized access failed, response is: $response" }
        val responseBodyAsString = response.body!!.string()
        return transferJsonToCompanyAssociatedDataEuTaxonomyDataForNonFinancials(responseBodyAsString)
    }
}
