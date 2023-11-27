package org.dataland.e2etests.tests

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

import org.dataland.communitymanager.openApiClient.infrastructure.Serializer.moshi
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.CompanyInformationWithT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class invalidSfdrRequestTests {
    private val apiAccessor = ApiAccessor()


    private fun convertJsonToList(jsonFile: File): List<CompanyInformationWithT<SfdrData>> {
        val jsonFileAsString = jsonFile.inputStream().bufferedReader().readText()
        val parameterizedTypeOfCompanyInformationWithT = Types
                .newParameterizedType(CompanyInformationWithT::class.java, SfdrData::class.java)
        val parameterizedTypeOfConverterOutput = Types
                .newParameterizedType(List::class.java, parameterizedTypeOfCompanyInformationWithT)
        val jsonAdapter: JsonAdapter<List<CompanyInformationWithT<SfdrData>>> =
                moshi.adapter(parameterizedTypeOfConverterOutput)
        return jsonAdapter.fromJson(jsonFileAsString)!!
    }

    @Test
    fun `post a company with invalid Sfdr data`() {

        val oneInvalidSfdrDataset = convertJsonToList(
                File("./build/resources/test/CompanyInformationWithSfdrPreparedFixtures.json"))
                .find{it.companyInformation.companyName=="Sfdr-dataset-with-invalid-currency-input"}!!

        val companyInformation = apiAccessor.uploadOneCompanyWithRandomIdentifier()

        val errorForInvalidInput = assertThrows<ServerException> {
            apiAccessor.sfdrUploaderFunction(companyInformation.actualStoredCompany.companyId,oneInvalidSfdrDataset.t,
                    "")
        }

        Assertions.assertTrue(errorForInvalidInput.message!!.contains("Server error : 500 "))
        Assertions.assertTrue(errorForInvalidInput.message!!.contains("MethodArgumentNotValidException: " +
                "Validation failed for argument"))

    }


}
