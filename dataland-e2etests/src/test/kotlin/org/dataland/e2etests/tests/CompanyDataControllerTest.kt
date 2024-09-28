package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.AggregatedFrameworkDataSummary
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.lang.Thread.sleep
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CompanyDataControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val baseCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithRandomIdentifiers(1)
            .first()
    private val checkOtherCompanyTrue = "Other Company true"
    private val checkOtherCompanyFalse = "Other Company false"
    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)
    private val fullPatchObject =
        CompanyInformationPatch(
            companyContactDetails = listOf("NewcompanyContactDetails@example.com"),
            companyName = "New-companyName",
            companyAlternativeNames = listOf("New-companyAlternativeNames"),
            companyLegalForm = "New-companyLegalForm",
            headquarters = "New-headquarters",
            headquartersPostalCode = "New-headquartersPostalCode",
            sector = "New-sector",
            countryCode = "New-countryCode",
            isTeaserCompany = false,
            website = "New-website",
            parentCompanyLei = "New-parentCompanyLei",
            identifiers =
                mapOf(
                    IdentifierType.Duns.value to listOf("Test-DUNS${UUID.randomUUID()}"),
                ),
        )

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a dummy company and check if post was successful`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        assertEquals(
            uploadInfo.inputCompanyInformation,
            uploadInfo.actualStoredCompany.companyInformation,
            "The company information in the post-response does not match " +
                "the actual information of the company to be posted.",
        )
        assertTrue(
            uploadInfo.actualStoredCompany.companyId.isNotEmpty(),
            "No valid company Id was assigned to the posted company.",
        )
    }

    @Test
    fun `post a dummy company and check if patching basic properties works as expected`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId

        val startingCompanyInformation = uploadInfo.inputCompanyInformation

        val patchObject =
            CompanyInformationPatch(
                companyName = startingCompanyInformation.companyName + "-UPDATED",
                website = "Updated Website",
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val updatedCompany =
            apiAccessor.companyDataControllerApi.patchCompanyById(
                companyId,
                patchObject,
            )
        assertEquals(
            patchObject.companyName, updatedCompany.companyInformation.companyName,
            "The company should have been updated",
        )
        assertEquals(
            patchObject.website, updatedCompany.companyInformation.website,
            "The website should have been set",
        )
        assertEquals(
            startingCompanyInformation.sector, updatedCompany.companyInformation.sector,
            "The sector should not have been changed",
        )
    }

    @Test
    fun `post a dummy company and check if patching identifiers works as expected`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyInformationToUpload =
            apiAccessor
                .testDataProviderEuTaxonomyForFinancials
                .getCompanyInformationWithRandomIdentifiers(1)
                .first()
        val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(companyInformationToUpload)
        val patchObject =
            CompanyInformationPatch(
                identifiers =
                    mapOf(
                        IdentifierType.Lei.value to listOf("Test-Lei1${UUID.randomUUID()}", "Test-Lei2${UUID.randomUUID()}"),
                        IdentifierType.Duns.value to listOf("Test-DUNS${UUID.randomUUID()}"),
                    ),
            )
        val updatedCompany =
            apiAccessor.companyDataControllerApi.patchCompanyById(uploadedCompany.companyId, patchObject)
        val oldIdentifiers = uploadedCompany.companyInformation.identifiers
        val newIdentifiers = updatedCompany.companyInformation.identifiers
        assertEquals(
            oldIdentifiers[IdentifierType.Isin.value], newIdentifiers[IdentifierType.Isin.value],
            "Unpatched identifiers should remain the same",
        )
        assertEquals(
            patchObject.identifiers!![IdentifierType.Lei.value], newIdentifiers[IdentifierType.Lei.value],
            "The update should work as expected",
        )
        assertEquals(
            patchObject.identifiers!![IdentifierType.Duns.value], newIdentifiers[IdentifierType.Duns.value],
            "The update should work as expected",
        )
    }

    @Test
    fun `post a dummy company and check if patching alternative names works as expected`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val patchObject =
            CompanyInformationPatch(
                companyAlternativeNames = listOf("Alt-Name-1", "Alt-Name-2"),
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val updatedCompany =
            apiAccessor.companyDataControllerApi.patchCompanyById(
                companyId,
                patchObject,
            )
        assertEquals(
            patchObject.companyAlternativeNames, updatedCompany.companyInformation.companyAlternativeNames,
            "The company alternative names should have been updated",
        )
    }

    @Test
    fun `post a dummy company and check if the putting mechanism for basic properties works as expected`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val putCompanyInformation =
            CompanyInformation(
                companyName = "Updated Name${UUID.randomUUID()}",
                headquarters = "Updated HQ${UUID.randomUUID()}",
                companyAlternativeNames = listOf("Alt-Name-1${UUID.randomUUID()}", "Alt-Name-2${UUID.randomUUID()}"),
                identifiers = mapOf(IdentifierType.Lei.value to listOf("Test-Lei${UUID.randomUUID()}")),
                countryCode = "DE",
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val updatedCompany = apiAccessor.companyDataControllerApi.putCompanyById(companyId, putCompanyInformation)
        assertEquals(
            putCompanyInformation.companyName, updatedCompany.companyInformation.companyName,
            "The company should have been updated",
        )
        assertEquals(
            putCompanyInformation.headquarters, updatedCompany.companyInformation.headquarters,
            "The headquarters should have been updated",
        )
        assertTrue(
            putCompanyInformation.companyAlternativeNames!!.toSet() ==
                updatedCompany.companyInformation.companyAlternativeNames!!.toSet(),
            "The company alternative names should have been updated",
        )
        assertEquals(
            null, updatedCompany.companyInformation.sector,
            "The sector should have been deleted",
        )
    }

    @Test
    fun `post a dummy company and check if the putting mechanism for identifiers works as expected`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val put1CompanyInformation =
            CompanyInformation(
                companyName = "Name",
                headquarters = "HQ",
                identifiers =
                    mapOf(
                        IdentifierType.Duns.value to listOf("Test-Duns${UUID.randomUUID()}", "Test-Duns2${UUID.randomUUID()}"),
                    ),
                countryCode = "DE",
            )
        val put2CompanyInformation =
            put1CompanyInformation.copy(
                identifiers = mapOf(IdentifierType.Lei.value to listOf("Test-Lei${UUID.randomUUID()}")),
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        var updatedCompany = apiAccessor.companyDataControllerApi.putCompanyById(companyId, put1CompanyInformation)
        assertTrue(
            put1CompanyInformation.identifiers[IdentifierType.Duns.value]!!.toSet() ==
                updatedCompany.companyInformation.identifiers[IdentifierType.Duns.value]!!.toSet() &&
                updatedCompany.companyInformation.identifiers[IdentifierType.Lei.value]!!.isEmpty(),
            "The Duns identifiers should have been updated and the Lei identifiers should still be empty",
        )
        updatedCompany = apiAccessor.companyDataControllerApi.putCompanyById(companyId, put2CompanyInformation)
        assertTrue(
            put2CompanyInformation.identifiers[IdentifierType.Lei.value]!!.toSet() ==
                updatedCompany.companyInformation.identifiers[IdentifierType.Lei.value]!!.toSet() &&
                updatedCompany.companyInformation.identifiers[IdentifierType.Duns.value]!!.isEmpty(),
            "The Lei identifiers should have been updated and the Duns identifiers should have been deleted",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its company Id`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val expectedStoredCompany =
            StoredCompany(
                uploadInfo.actualStoredCompany.companyId,
                uploadInfo.inputCompanyInformation,
                emptyList(),
            )
        assertEquals(
            expectedStoredCompany.copy(
                companyInformation =
                    expectedStoredCompany
                        .companyInformation
                        .copy(
                            companyContactDetails =
                                expectedStoredCompany
                                    .companyInformation.companyContactDetails
                                    ?.sorted(),
                        ),
            ),
            apiAccessor.companyDataControllerApi.getCompanyById(uploadInfo.actualStoredCompany.companyId),
            "Dataland does not contain the posted company.",
        )
    }

    @Test
    fun `post two dummy companies with framework data and check if the distinct endpoint returns all values`() {
        val listOfTestCompanyInformation =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getCompanyInformationWithoutIdentifiers(2)
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            1,
        )
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors.containsAll(listOfTestCompanyInformation.mapNotNull { it.sector }),
            "The list of all occurring sectors does not contain the sectors of the posted companies.",
        )
        assertTrue(
            distinctValues.countryCodes.containsAll(listOfTestCompanyInformation.map { it.countryCode }),
            "The list of all occurring country codes does not contain the country codes of the posted companies.",
        )
    }

    @Test
    fun `post a dummy company as teaser company and test if it is retrievable by company ID as unauthorized user`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(true)
        val getCompanyByIdResponse =
            apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
                uploadInfo.actualStoredCompany.companyId,
            )
        val expectedStoredTeaserCompany =
            StoredCompany(
                companyId = uploadInfo.actualStoredCompany.companyId,
                companyInformation = uploadInfo.inputCompanyInformation,
                dataRegisteredByDataland = emptyList(),
            )
        assertEquals(
            expectedStoredTeaserCompany.copy(
                companyInformation =
                    expectedStoredTeaserCompany
                        .companyInformation
                        .copy(
                            companyContactDetails =
                                expectedStoredTeaserCompany
                                    .companyInformation.companyContactDetails
                                    ?.sorted(),
                        ),
            ),
            getCompanyByIdResponse,
            "The posted company does not equal the teaser company.",
        )
    }

    @Test
    fun `post a dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val exception =
            assertThrows<IllegalArgumentException> {
                apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
                    uploadInfo.actualStoredCompany.companyId,
                )
            }
        assertTrue(
            exception.message!!.contains("Unauthorized access failed"),
            "The exception message does not say that an unauthorized access was the cause.",
        )
    }

    @Test
    fun `post a dummy company as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getCompanyInformationWithoutIdentifiers(1)
                .first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi
                    .postCompany(testCompanyInformation)
                    .companyId
            }
        assertEquals(
            "Client error : 403 ",
            exception.message,
            "The exception message does not say that a 403 client error was the cause.",
        )
    }

    @Test
    fun `post a dummy company twice and receive the expected error code and message`() {
        val testCompanyInformation =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
                .getCompanyInformationWithRandomIdentifiers(1)
                .first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val storedCompany = apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)

        val storedCompanyIdentifier =
            storedCompany
                .companyInformation.identifiers.entries
                .first { it.value.isNotEmpty() }

        val response =
            apiAccessor.companyDataControllerApi.postCompanyWithHttpInfo(testCompanyInformation)
                as ClientError

        assertEquals(
            400,
            response.statusCode,
            "The status code is ${response.statusCode} instead of the expected 400.",
        )
        assertTrue(
            response.body
                .toString()
                .contains("At least one of the identifiers you entered is already being used by another company"),
            "The response message is not as expected.",
        )
        assertTrue(
            response.body.toString().contains(storedCompanyIdentifier.value.first()),
            "The response message does not contain the duplicate identifier.",
        )
    }

    @Test
    fun `check if the new companies search via name and ids endpoint works as expected`() {
        val testString = "unique-test-string-${UUID.randomUUID()}"
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        uploadCompaniesInReverseToExpectedOrder(testString)
        sleep(2000)
        val sortedCompanyNames =
            apiAccessor.companyDataControllerApi
                .getCompaniesBySearchString(
                    searchString = testString,
                ).map { it.companyName }
        assertEquals(
            listOf("$testString true", checkOtherCompanyTrue, "$testString none"),
            sortedCompanyNames.filter { it.contains("none") || it.contains("true") },
        )
        assertEquals(
            listOf("$testString true", checkOtherCompanyTrue, "$testString false"),
            sortedCompanyNames.filter { it.contains("$testString false") || it.contains("true") },
        )
        assertEquals(
            listOf("$testString true", checkOtherCompanyTrue, checkOtherCompanyFalse),
            sortedCompanyNames.filter { it.contains(checkOtherCompanyFalse) || it.contains("true") },
        )
    }

    private fun uploadCompaniesInReverseToExpectedOrder(expectedSearchString: String) {
        uploadModifiedBaseCompany("$expectedSearchString none", null)
        var companyId = uploadModifiedBaseCompany("$expectedSearchString false", null)
        uploadDummyDataset(companyId = companyId, bypassQa = false)
        companyId = uploadModifiedBaseCompany("$expectedSearchString true", null)
        uploadDummyDataset(companyId = companyId, bypassQa = true)
        companyId = uploadModifiedBaseCompany(checkOtherCompanyFalse, listOf("1${expectedSearchString}2"))
        uploadDummyDataset(companyId = companyId, bypassQa = false)
        companyId = uploadModifiedBaseCompany(checkOtherCompanyTrue, listOf("1${expectedSearchString}2"))
        uploadDummyDataset(companyId = companyId, bypassQa = true)
    }

    private fun uploadModifiedBaseCompany(
        name: String,
        alternativeNames: List<String>?,
    ): String {
        val companyInformation =
            baseCompanyInformation.copy(
                companyName = name,
                companyAlternativeNames = alternativeNames,
                identifiers =
                    mapOf(
                        IdentifierType.Isin.value to listOf(UUID.randomUUID().toString()),
                    ),
            )
        return apiAccessor.companyDataControllerApi.postCompany(companyInformation).companyId
    }

    val dummyCompanyAssociatedDataWithoutCompanyId =
        CompanyAssociatedDataEutaxonomyNonFinancialsData(
            companyId = "placeholder",
            reportingPeriod = "placeholder",
            data = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first(),
        )

    private fun uploadDummyDataset(
        companyId: String,
        reportingPeriod: String = "default",
        bypassQa: Boolean = false,
    ) {
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEutaxonomyNonFinancialsData(
            dummyCompanyAssociatedDataWithoutCompanyId.copy(companyId = companyId, reportingPeriod = reportingPeriod),
            bypassQa,
        )
    }

    @Test
    fun `counts the number of datasets for a company`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = uploadModifiedBaseCompany("AggregatedInformation", null)
        uploadDummyDataset(companyId = companyId, reportingPeriod = "2022", bypassQa = true)
        uploadDummyDataset(companyId = companyId, reportingPeriod = "2021", bypassQa = true)
        sleep(100)
        val exceptionMap: Map<DataTypeEnum, Long> = mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to 2)
        val expectedMap =
            DataTypeEnum.entries.associate { framework ->
                val numOfReportingPeriods = exceptionMap[framework] ?: 0
                framework.toString() to
                    AggregatedFrameworkDataSummary(
                        numberOfProvidedReportingPeriods = numOfReportingPeriods,
                    )
            }
        val aggregatedFrameworkDataSummary =
            apiAccessor.companyDataControllerApi
                .getAggregatedFrameworkDataSummary(
                    companyId = companyId,
                ).toSortedMap()

        assertEquals(
            expectedMap,
            aggregatedFrameworkDataSummary,
        )
    }

    @Test
    fun `post a dummy company and check if it can be retrieved by the companiesInfo endpoint`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val expectedCompanyInformation = uploadInfo.inputCompanyInformation
        assertEquals(
            expectedCompanyInformation.copy(
                companyContactDetails =
                    expectedCompanyInformation
                        .companyContactDetails
                        ?.sorted(),
            ),
            apiAccessor.companyDataControllerApi.getCompanyInfo(uploadInfo.actualStoredCompany.companyId),
            "Dataland does not contain the posted company.",
        )
    }

    @Test
    fun `check that an exception is thrown if the contactDetails does not have the valid format`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val patchObject =
            CompanyInformationPatch(
                companyContactDetails = listOf("Email-without-proper-format"),
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)

        assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.patchCompanyById(
                companyId,
                patchObject,
            )
        }
    }

    @Test
    fun `check that the Dataland uploader can patch contactDetails and website of a company without an owner`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val patchObject =
            CompanyInformationPatch(
                companyContactDetails = listOf("Email1@example.com", "Email2@example.com"),
                website = "New-Website",
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val updatedCompany =
            apiAccessor.companyDataControllerApi.patchCompanyById(
                companyId,
                patchObject,
            )
        assertEquals(
            patchObject.companyContactDetails, updatedCompany.companyInformation.companyContactDetails,
            "The company contact details should have been updated",
        )
        assertEquals(
            patchObject.website, updatedCompany.companyInformation.website,
            "The company website should have been updated",
        )
    }

    @Test
    fun `check that the Dataland uploader cannot patch contactDetails or website if the company has a companyOwner`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val ownerId = UUID.fromString("18b67ecc-1176-4506-8414-1e81661017ca")
        val originalCompany = uploadInfo.actualStoredCompany
        val companyId = originalCompany.companyId

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            UUID.fromString(companyId),
            ownerId,
        )

        val patchObject =
            CompanyInformationPatch(
                companyContactDetails = listOf("Email3@example.com"),
                website = "New-Website-2",
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.patchCompanyById(
                    companyId,
                    patchObject,
                )
            }

        assertTrue(exception.statusCode == 403, "The exception should indicate unauthorized access (HTTP 403)")

        val companyAfterAttempt = apiAccessor.companyDataControllerApi.getCompanyById(companyId)
        assertEquals(
            originalCompany.companyInformation.companyContactDetails?.sorted(),
            companyAfterAttempt.companyInformation.companyContactDetails,
            "The company contact details should not have been updated",
        )
    }

    @Test
    fun `check that the Dataland uploader cannot patch unallowed fields`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val originalCompany = uploadInfo.actualStoredCompany
        val companyId = originalCompany.companyId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.patchCompanyById(
                companyId,
                fullPatchObject,
            )
        }

        val companyAfterAttempt = apiAccessor.companyDataControllerApi.getCompanyById(companyId)
        assertEquals(
            originalCompany.companyInformation.companyContactDetails?.sorted(),
            companyAfterAttempt.companyInformation.companyContactDetails,
            "The company contact details should not have been updated",
        )
        assertEquals(
            originalCompany.companyInformation.companyName,
            companyAfterAttempt.companyInformation.companyName,
            "The company name should not have been updated",
        )
    }

    @Test
    fun `check that the a company owner can patch contact details and website of their own company`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val originalCompany = uploadInfo.actualStoredCompany
        val companyId = originalCompany.companyId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            UUID.fromString(companyId),
            dataReaderUserId,
        )

        val patchObject =
            CompanyInformationPatch(
                website = "New-Website-3",
                companyContactDetails = listOf("Email1@example.com"),
            )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val updatedCompany =
            apiAccessor.companyDataControllerApi.patchCompanyById(
                companyId,
                patchObject,
            )
        assertEquals(
            patchObject.companyContactDetails, updatedCompany.companyInformation.companyContactDetails,
            "The company contact details should have been updated",
        )
        assertEquals(
            patchObject.website, updatedCompany.companyInformation.website,
            "The company name should have been updated by the company owner",
        )
    }

    @Test
    fun `check that the company owner cannot patch unallowed fields`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val originalCompany = uploadInfo.actualStoredCompany
        val companyId = originalCompany.companyId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner,
            UUID.fromString(companyId),
            dataReaderUserId,
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.patchCompanyById(
                    companyId,
                    fullPatchObject,
                )
            }
        val responseBody = (exception.response as ClientError<*>).body as String
        assertTrue(
            responseBody.contains(
                "You do not have the required permission to change the following fields",
            ),
        )
    }

    @Test
    fun `check that patching with an invalid company ID and invalid patch fields returns an exception`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)

        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.patchCompanyById(
                    UUID.randomUUID().toString(),
                    fullPatchObject,
                )
            }

        val responseBody = (exception.response as ClientError<*>).body as String
        assertTrue(
            responseBody.contains(
                "Dataland does not know the company ID",
            ),
        )
    }
}
