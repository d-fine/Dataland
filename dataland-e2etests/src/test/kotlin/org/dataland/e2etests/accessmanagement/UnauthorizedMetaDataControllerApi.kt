package org.dataland.e2etests.accessmanagement

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
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
            .url("$BASE_PATH_TO_DATALAND_PROXY/metadata/$dataId")
            .get()
            .build()
    }

    private fun buildGetListOfDataMetaInfoRequest(companyId: String, dataType: String): Request {
        val endpointUrl = HttpUrl.Builder()
            .scheme(BASE_PATH_TO_DATALAND_PROXY.substringBefore("://"))
            .host(BASE_PATH_TO_DATALAND_PROXY.substringAfter("//").substringBefore(":"))
            .port(BASE_PATH_TO_DATALAND_PROXY.substringAfter(":").substringAfter(":").substringBefore("/api").toInt())
            .addPathSegment("api")
            .addPathSegment("metadata")
            .addQueryParameter("companyId", companyId)
            .addQueryParameter("dataType", dataType)
            .build()
        return Request.Builder()
            .url(endpointUrl)
            .get()
            .build()
    }

    fun getDataMetaInfo(dataId: String): DataMetaInformation {
        val response = client.newCall(buildGetDataMetaInfoRequest(dataId)).execute()
        if (!response.isSuccessful) throw IllegalArgumentException("Unauthorized access failed, response is: $response")
        val responseBodyAsString = response.body!!.string()
        return transferJsonToDataMetaInformation(responseBodyAsString)
    }

    fun getListOfDataMetaInfo(companyId: String, dataType: String): List<DataMetaInformation> {
        val response = client.newCall(buildGetListOfDataMetaInfoRequest(companyId, dataType)).execute()
        if (!response.isSuccessful) throw IllegalArgumentException("Unauthorized access failed, response is: $response")
        val responseBodyAsString = response.body!!.string()
        return transferJsonToListOfDataMetaInformation(responseBodyAsString)
    }
}
