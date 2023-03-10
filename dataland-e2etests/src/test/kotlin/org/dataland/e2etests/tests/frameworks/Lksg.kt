package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class Lksg {

    private val apiAccessor = ApiAccessor()

    private val listOfOneLksgDataSet = apiAccessor.testDataProviderForLksgData.getTData(1)
    private val listOfOneCompanyInformation = apiAccessor.testDataProviderForLksgData
        .getCompanyInformationWithoutIdentifiers(1)

    @Test
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val listOfUploadInfo = apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
            listOfOneCompanyInformation,
            listOfOneLksgDataSet,
            apiAccessor.lksgUploaderFunction,
        )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData = apiAccessor.dataControllerApiForLksgData
            .getCompanyAssociatedLksgData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType = apiAccessor.metaDataControllerApi
            .getDataMetaInfo(receivedDataMetaInformation.dataId).dataType

        assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        assertEquals(listOfOneLksgDataSet[0], downloadedAssociatedData.data)
    }

    @Test
    fun `check that reporting period and version history parameters of GET endpoint for companies work correctly`() {
//        Upload multiple lksg datasets for multiple reporting periods for the same company
        val uploadLambda = { companyId: String, lksgData: LksgData, reportingDate: String ->
            val body = CompanyAssociatedDataLksgData(companyId, reportingDate, lksgData)
            apiAccessor.dataControllerApiForLksgData.postCompanyAssociatedLksgData(body)
        }

        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val lksgData = apiAccessor.testDataProviderForLksgData.getTData(2)
        uploadLambda(companyId, lksgData[0], "2022")
        uploadLambda(companyId, lksgData[0], "2023")
        Thread.sleep(1000)
        uploadLambda(companyId, lksgData[1], "2022")
        uploadLambda(companyId, lksgData[1], "2023")

        // Retrieve uploaded data set from the GET lksg_companies_companyId endpoint and assert that the correct ones
        // are retrieved
        val responseWithoutVersioning = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = true,
        )
        val responseWithVersioning = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = false,
        )
        val response2023WithoutVersioning = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = true,
            reportingPeriod = "2023",
        )
        val response2023WithVersioning = apiAccessor.dataControllerApiForLksgData.getAllCompanyLksgData(
            companyId = companyId,
            showOnlyActive = false,
            reportingPeriod = "2023",
        )
        assertTrue(
            responseWithoutVersioning.size == 2 &&
                responseWithVersioning.size == 4 &&
                response2023WithVersioning.size == 2 &&
                response2023WithoutVersioning.size == 1,
            "Versioning and reporting Period parameters should influence the number of returned datasets " +
                "correctly but they do not.",
        )
        assertEquals(
            response2023WithoutVersioning[0].data,
            lksgData[1],
            "The active dataset should equal the latest uploaded LKSG dataset"
        )
    }
}
