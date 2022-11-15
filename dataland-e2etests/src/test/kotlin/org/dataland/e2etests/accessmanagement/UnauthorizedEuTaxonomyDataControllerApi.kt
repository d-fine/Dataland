package org.dataland.e2etests.accessmanagement

import com.squareup.moshi.JsonAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND

class UnauthorizedEuTaxonomyDataControllerApi {

    private val client = OkHttpClient()

    private fun transferJsonToCompanyAssociatedDataEuTaxonomyDataForNonFinancials(inputString: String):
        CompanyAssociatedDataEuTaxonomyDataForNonFinancials {
        val jsonAdapter: JsonAdapter<CompanyAssociatedDataEuTaxonomyDataForNonFinancials> =
            moshi.adapter(CompanyAssociatedDataEuTaxonomyDataForNonFinancials::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun buildGetCompanyAssociatedDataEuTaxonomyDataForNonFinancialsRequest(dataId: String): Request {
        return Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/eutaxonomy-non-financials/$dataId")
            .get()
            .build()
    }

    fun getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(dataId: String):
        CompanyAssociatedDataEuTaxonomyDataForNonFinancials {
        val response = client.newCall(buildGetCompanyAssociatedDataEuTaxonomyDataForNonFinancialsRequest(dataId))
            .execute()
        if (!response.isSuccessful) throw IllegalArgumentException("Unauthorized access failed, response is: $response")
        val responseBodyAsString = response.body.string()
        return transferJsonToCompanyAssociatedDataEuTaxonomyDataForNonFinancials(responseBodyAsString)
    }
}
