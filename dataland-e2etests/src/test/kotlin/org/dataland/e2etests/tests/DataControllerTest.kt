package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.BaseDataPointYesNo
import org.dataland.datalandbackend.openApiClient.model.BaseDocumentReference
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.LksgAttachment
import org.dataland.datalandbackend.openApiClient.model.LksgAttachmentAttachment
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.LksgGeneral
import org.dataland.datalandbackend.openApiClient.model.LksgGeneralMasterData
import org.dataland.datalandbackend.openApiClient.model.YesNo
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.time.LocalDate

class DataControllerTest {

    private val apiAccessor = ApiAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    private val testCompanyInformationNonTeaser =
        testCompanyInformation.copy(isTeaserCompany = false)
    private val testCompanyInformationTeaser =
        testCompanyInformation.copy(isTeaserCompany = true)

    @Test
    fun `post a dummy company with lksg data and check if invalid document references are allowed`() {
        val companyInfo = apiAccessor.uploadOneCompanyWithRandomIdentifier()

        val correctFileReference = "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63"
        val wrongFileReference = "abcd"

        val minimalLksgData = LksgData(
            general = LksgGeneral(
                masterData =
                LksgGeneralMasterData(dataDate = LocalDate.of(2022, 5, 20)),
            ),
            attachment = LksgAttachment(
                attachment = LksgAttachmentAttachment(
                    attachment = BaseDataPointYesNo(
                        value = YesNo.Yes,
                        dataSource = BaseDocumentReference(
                            fileReference = correctFileReference,
                            fileName = "Test name",
                        ),
                    ),
                ),
            ),
        )

        val minimalLksgDataFail = LksgData(
            general = LksgGeneral(
                masterData =
                LksgGeneralMasterData(dataDate = LocalDate.of(2022, 5, 20)),
            ),
            attachment = LksgAttachment(
                attachment = LksgAttachmentAttachment(
                    attachment = BaseDataPointYesNo(
                        value = YesNo.Yes,
                        dataSource = BaseDocumentReference(
                            fileReference = wrongFileReference,
                            fileName = "Test name",
                        ),
                    ),
                ),
            ),
        )

        var companyAssociatedLksgData = CompanyAssociatedDataLksgData(
            companyInfo.actualStoredCompany.companyId,
            "2018",
            minimalLksgData,
        )
        var companyAssociatedLksgDataFail = CompanyAssociatedDataLksgData(
            companyInfo.actualStoredCompany.companyId,
            "2019",
            minimalLksgDataFail,
        )

        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataControllerApiForLksgData.postCompanyAssociatedLksgData(companyAssociatedLksgDataFail)
            }
        assertEquals("Client error : 400 ", exception.message)

        assertDoesNotThrow {
            apiAccessor.dataControllerApiForLksgData.postCompanyAssociatedLksgData(companyAssociatedLksgData)
        }
    }

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be retrieved`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )
        val companyAssociatedDataEuTaxonomyDataForNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEutaxonomyNonFinancialsData(mapOfIds.getValue("dataId"))

        assertEquals(
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds.getValue("companyId"),
                "",
                testDataEuTaxonomyNonFinancials,
            ),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and/or their company IDs are not equal.",
        )
    }

    @Test
    fun `post a dummy company as teaser company and a data set for it and test if unauthorized access is possible`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationTeaser,
            testDataEuTaxonomyNonFinancials,
        )
        val getDataByIdResponse = apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
            .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds.getValue("dataId"))
        val expectedCompanyAssociatedData = CompanyAssociatedDataEutaxonomyNonFinancialsData(
            mapOfIds.getValue("companyId"),
            "",
            testDataEuTaxonomyNonFinancials,
        )
        assertEquals(
            expectedCompanyAssociatedData,
            getDataByIdResponse,
            "The posted data does not equal the expected test data.",
        )
    }

    @Test
    fun `post a dummy company and a data set for it and test if unauthorized access is denied`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationNonTeaser,
            testDataEuTaxonomyNonFinancials,
        )
        val exception = assertThrows<IllegalArgumentException> {
            apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
                .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds.getValue("dataId"))
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post data as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                    .postCompanyAssociatedEutaxonomyNonFinancialsData(
                        CompanyAssociatedDataEutaxonomyNonFinancialsData(
                            testCompanyId,
                            "",
                            testDataEuTaxonomyNonFinancials,
                        ),
                        true,
                    )
            }
        assertEquals("Client error : 403 ", exception.message)
    }
}
