package org.dataland.e2etests.customApiControllers

import com.squareup.moshi.JsonAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.dataland.datalandbackend.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.springframework.core.io.InputStreamResource
import java.io.File

class CustomSmeDataControllerApi(private val token: String) {

    private val client = OkHttpClient()

    private fun transferJsonToDataMetaInformation(inputString: String):
        DataMetaInformation {
        val jsonAdapter: JsonAdapter<DataMetaInformation> =
            moshi.adapter(DataMetaInformation::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }
    private fun transferJsonToCompanyAssociatedDataSmeData(inputString: String):
            CompanyAssociatedDataSmeData {
        val jsonAdapter: JsonAdapter<CompanyAssociatedDataSmeData> =
            moshi.adapter(CompanyAssociatedDataSmeData::class.java)
        return jsonAdapter.fromJson(inputString)!!
    }

    private fun transferCompanyAssociatedDataSmeDataToJson(input: CompanyAssociatedDataSmeData):
        String {
        val jsonAdapter: JsonAdapter<CompanyAssociatedDataSmeData> =
            moshi.adapter(CompanyAssociatedDataSmeData::class.java)
        return jsonAdapter.toJson(input)
    }

    private fun buildRequestForPostingCompanyAssociatedSmeData(
        companyAssociatedSmeData: CompanyAssociatedDataSmeData,
        documents: List<File>,
    ): Request {
        val requestBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "companyAssociatedSmeData",
                null,
                transferCompanyAssociatedDataSmeDataToJson(companyAssociatedSmeData).toRequestBody(),
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
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/sme")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()
    }

    fun postCompanyAssociatedDataSmeData(
        companyAssociatedSmeData: CompanyAssociatedDataSmeData,
        documents: List<File>,
    ):
        DataMetaInformation {
        val response = client.newCall(
            buildRequestForPostingCompanyAssociatedSmeData(
                companyAssociatedSmeData,
                documents,
            ),
        ).execute()
        require(response.isSuccessful) { "Request failed, response is: $response" }
        val responseBodyAsString = response.body!!.string()
        return transferJsonToDataMetaInformation(responseBodyAsString)
    }

    fun getCompanyAssociatedDataSmeData(
        dataId: String,
    ):
            CompanyAssociatedDataSmeData {
        val response = client.newCall(Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/sme/$dataId")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        ).execute()
        require(response.isSuccessful) { "Request failed, response is: $response" }
        val responseBodyAsString = response.body!!.string()
        return transferJsonToCompanyAssociatedDataSmeData(responseBodyAsString)
    }
    /*fun getPrivateDocument(
        dataId: String,
        hash: String,
    ):
            InputStreamResource {
        val response = client.newCall(Request.Builder()
            .url("$BASE_PATH_TO_DATALAND_BACKEND/data/sme/$dataId&$hash")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()
        ).execute()
        require(response.isSuccessful) { "Request failed, response is: $response" }
        val responseBodyAsString = response.body
        return InputStreamResource(responseBodyAsString)
    }
    */
}
