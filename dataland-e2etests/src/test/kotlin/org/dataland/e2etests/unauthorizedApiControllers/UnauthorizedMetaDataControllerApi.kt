package org.dataland.e2etests.unauthorizedApiControllers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import java.lang.reflect.ParameterizedType

class UnauthorizedMetaDataControllerApi {

    private val client = OkHttpClient()

    private fun transferJsonToDataMetaInformation(inputString: String): DataMetaInformation {
        val jsonAdapter: JsonAdapter<DataMetaInformation> = moshi.adapter(DataMetaInformation::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun transferJsonToListOfDataMetaInformation(inputString: String): List<DataMetaInformation> {
        val parameterizedType: ParameterizedType =
            Types.newParameterizedType(List::class.java, DataMetaInformation::class.java)
        val jsonAdapter: JsonAdapter<List<DataMetaInformation>> = moshi.adapter(parameterizedType)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun buildGetDataMetaInfoRequest(dataId: String): Request {
        return Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/metadata/$dataId")
            .get()
            .build()
    }

    private fun buildGetListOfDataMetaInfoRequest(companyId: String, dataType: DataTypeEnum): Request {
        val endpointUrl = BASE_PATH_TO_DATALAND_BACKEND
            .toHttpUrl().newBuilder()
            .addPathSegment("metadata")
            .addQueryParameter("companyId", companyId)
            .addQueryParameter("dataType", dataType.value)
            .build()
        return Request.Builder()
            .url(endpointUrl)
            .get()
            .build()
    }

    fun getDataMetaInfo(dataId: String): DataMetaInformation {
        val response = client.newCall(buildGetDataMetaInfoRequest(dataId)).execute()
        require(response.isSuccessful) { "Unauthorized access failed, response is: $response" }
        val responseBodyAsString = response.body!!.string()
        return transferJsonToDataMetaInformation(responseBodyAsString)
    }

    fun getListOfDataMetaInfo(companyId: String, dataType: DataTypeEnum): List<DataMetaInformation> {
        val response = client.newCall(buildGetListOfDataMetaInfoRequest(companyId, dataType)).execute()
        require(response.isSuccessful) { "Unauthorized access failed, response is: $response" }
        val responseBodyAsString = response.body!!.string()
        return transferJsonToListOfDataMetaInformation(responseBodyAsString)
    }
}
