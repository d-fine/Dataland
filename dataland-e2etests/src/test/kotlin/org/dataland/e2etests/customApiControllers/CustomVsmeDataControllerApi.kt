package org.dataland.e2etests.customApiControllers

import com.squareup.moshi.JsonAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataVsmeData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import java.io.File

class CustomVsmeDataControllerApi(private val token: String) {

    private val client = OkHttpClient()

    private fun transferJsonToDataMetaInformation(inputString: String):
        DataMetaInformation {
        val jsonAdapter: JsonAdapter<DataMetaInformation> =
            moshi.adapter(DataMetaInformation::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun transferCompanyAssociatedDataVsmeDataToJson(input: CompanyAssociatedDataVsmeData):
        String {
        val jsonAdapter: JsonAdapter<CompanyAssociatedDataVsmeData> =
            moshi.adapter(CompanyAssociatedDataVsmeData::class.java)
        return jsonAdapter.toJson(input)
    }

    private fun buildRequestForPostingCompanyAssociatedVsmeData(
        companyAssociatedVsmeData: CompanyAssociatedDataVsmeData,
        documents: List<File>,
    ): Request {
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "companyAssociatedVsmeData",
                null,
                transferCompanyAssociatedDataVsmeDataToJson(companyAssociatedVsmeData).toRequestBody(),
            )

        documents.forEach { file ->
            requestBodyBuilder.addFormDataPart(
                "documents",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull()),
            )
        }

        val requestBody = requestBodyBuilder.build()

        return Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/vsme")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()
    }

    fun postCompanyAssociatedDataVsmeData(
        companyAssociatedVsmeData: CompanyAssociatedDataVsmeData,
        documents: List<File>,
    ):
        DataMetaInformation {
        val response = client.newCall(
            buildRequestForPostingCompanyAssociatedVsmeData(
                companyAssociatedVsmeData,
                documents,
            ),
        ).execute()
        if (response.code == 403) { throw ClientException("Client error : 403 ") }
        require(response.isSuccessful) { "Request failed with unexpected reason, response is: $response" }
        val responseBodyAsString = response.body!!.string()
        return transferJsonToDataMetaInformation(responseBodyAsString)
    }
}
