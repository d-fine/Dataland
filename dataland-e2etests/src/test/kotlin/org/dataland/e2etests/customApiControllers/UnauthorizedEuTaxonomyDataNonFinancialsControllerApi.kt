package org.dataland.e2etests.customApiControllers

import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.utils.api.ApiAwait
import org.springframework.http.HttpStatus

class UnauthorizedEuTaxonomyDataNonFinancialsControllerApi {
    private val client = OkHttpClient()

    private fun buildGetCompanyAssociatedDataEuTaxonomyDataForNonFinancialsRequest(dataId: String): Request =
        Request
            .Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/eutaxonomy-non-financials/$dataId")
            .get()
            .build()

    fun getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(dataId: String): CompanyAssociatedDataEutaxonomyNonFinancialsData {
        val response =
            ApiAwait.waitForData(
                timeoutInSeconds = 10L,
                retryOnHttpErrors = setOf(HttpStatus.FORBIDDEN), condition = { it.code == HttpStatus.OK.value() },
            ) {
                client
                    .newCall(buildGetCompanyAssociatedDataEuTaxonomyDataForNonFinancialsRequest(dataId))
                    .execute()
            }
        return defaultObjectMapper.readValue(
            response.body!!.string(),
            CompanyAssociatedDataEutaxonomyNonFinancialsData::class.java,
        )
    }
}
