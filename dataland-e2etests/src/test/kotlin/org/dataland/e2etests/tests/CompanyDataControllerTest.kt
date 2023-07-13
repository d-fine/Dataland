package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class CompanyDataControllerTest {

    private val apiAccessor = ApiAccessor()
    private val baseCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithRandomIdentifiers(1).first()
    private val company2 = "Company 2"
    private val company3 = "Company 3"
    private val company5 = "Company 5"
    private val company6 = "Company 6"
    private val company8 = "Company 8"
    private val company9 = "Company 9"

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

        val patchObject = CompanyInformationPatch(
            companyName = startingCompanyInformation.companyName + "-UPDATED",
            website = "Updated Website",
        )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val updatedCompany = apiAccessor.companyDataControllerApi.patchCompanyById(
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
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val companyInformationToUpload = apiAccessor
            .testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithRandomIdentifiers(1)
            .first()
        val uploadedCompany = apiAccessor.companyDataControllerApi.postCompany(companyInformationToUpload)

        val patchObject = CompanyInformationPatch(
            identifiers = mapOf(
                IdentifierType.lei.value to listOf("Test-Lei1${UUID.randomUUID()}", "Test-Lei2${UUID.randomUUID()}"),
                IdentifierType.duns.value to listOf("Test-DUNS${UUID.randomUUID()}"),
            ),
        )

        val updatedCompany = apiAccessor.companyDataControllerApi.patchCompanyById(
            uploadedCompany.companyId,
            patchObject,
        )

        val oldIdentifiers = uploadedCompany.companyInformation.identifiers
        val newIdentifiers = updatedCompany.companyInformation.identifiers
        assertEquals(
            oldIdentifiers[IdentifierType.isin.value], newIdentifiers[IdentifierType.isin.value],
            "Unpatched identifiers should remain the same",
        )
        assertEquals(
            patchObject.identifiers!![IdentifierType.lei.value], newIdentifiers[IdentifierType.lei.value],
            "The update should work as expected",
        )
        assertEquals(
            patchObject.identifiers!![IdentifierType.duns.value], newIdentifiers[IdentifierType.duns.value],
            "The update should work as expected",
        )
    }

    @Test
    fun `post a dummy company and check if patching alternative names works as expected`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val patchObject = CompanyInformationPatch(
            companyAlternativeNames = listOf("Alt-Name-1", "Alt-Name-2"),
        )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val updatedCompany = apiAccessor.companyDataControllerApi.patchCompanyById(
            companyId,
            patchObject,
        )
        assertEquals(
            patchObject.companyAlternativeNames!!, updatedCompany.companyInformation.companyAlternativeNames,
            "The company alternative names should have been updated",
        )
    }

    @Test
    fun `post a dummy company and check if the putting mechanism for basic properties works as expected`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val putCompanyInformation = CompanyInformation(
            companyName = "Updated Name${UUID.randomUUID()}",
            headquarters = "Updated HQ${UUID.randomUUID()}",
            companyAlternativeNames = listOf("Alt-Name-1${UUID.randomUUID()}", "Alt-Name-2${UUID.randomUUID()}"),
            identifiers = mapOf(
                IdentifierType.lei.value to listOf("Test-Lei${UUID.randomUUID()}"),
            ),
            countryCode = "DE",
        )
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val updatedCompany = apiAccessor.companyDataControllerApi.putCompanyById(
            companyId,
            putCompanyInformation,
        )
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
        // TODO should upload a company with e.g. DUNS and then check that DUNS is gone
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        val companyId = uploadInfo.actualStoredCompany.companyId
        val put1CompanyInformation = CompanyInformation(
            companyName = "Name",
            headquarters = "HQ",
            identifiers = mapOf(
                IdentifierType.duns.value to listOf("Test-Duns${UUID.randomUUID()}", "Test-Duns2${UUID.randomUUID()}"),
            ),
            countryCode = "DE",
        )
        val put2CompanyInformation = put1CompanyInformation.copy(
            identifiers = mapOf(
                IdentifierType.lei.value to listOf("Test-Lei${UUID.randomUUID()}", "Test-Lei2${UUID.randomUUID()}"),
            ),
        )

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        var updatedCompany = apiAccessor.companyDataControllerApi.putCompanyById(companyId, put1CompanyInformation)
        assertTrue(
            put1CompanyInformation.identifiers[IdentifierType.duns.value]!!.toSet() ==
                updatedCompany.companyInformation.identifiers[IdentifierType.duns.value]!!.toSet() &&
                updatedCompany.companyInformation.identifiers[IdentifierType.lei.value]!!.isEmpty(),
            "The Duns identifiers should have been updated and the Lei identifiers should still be empty",
        )
        updatedCompany = apiAccessor.companyDataControllerApi.putCompanyById(companyId, put2CompanyInformation)
        assertTrue(
            put2CompanyInformation.identifiers[IdentifierType.lei.value]!!.toSet() ==
                updatedCompany.companyInformation.identifiers[IdentifierType.lei.value]!!.toSet() &&
                updatedCompany.companyInformation.identifiers[IdentifierType.duns.value]!!.isEmpty(),
            "The Lei identifiers should have been updated and the Duns identifiers should have been deleted",
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its company Id`() {
        val uploadInfo = apiAccessor.uploadNCompaniesWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val expectedStoredCompany = StoredCompany(
            uploadInfo.actualStoredCompany.companyId,
            uploadInfo.inputCompanyInformation,
            emptyList(),
        )
        assertEquals(
            expectedStoredCompany,
            apiAccessor.companyDataControllerApi.getCompanyById(uploadInfo.actualStoredCompany.companyId),
            "Dataland does not contain the posted company.",
        )
    }

    @Test
    fun `post two dummy companies with framework data and check if the distinct endpoint returns all values`() {
        val listOfTestCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(2)
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOf(DataTypeEnum.eutaxonomyMinusNonMinusFinancials to listOfTestCompanyInformation),
            1,
        )
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors.containsAll(listOfTestCompanyInformation.map { it.sector }),
            "The list of all occurring sectors does not contain the sectors of the posted companies.",
        )
        assertTrue(
            distinctValues.countryCodes.containsAll(listOfTestCompanyInformation.map { it.countryCode }),
            "The list of all occurring country codes does not contain the country codes of the posted companies.",
        )
    }

    @Test
    fun `post dummy companies with frontendExcluded framework data and check if the distinct endpoint ignores`() {
        val mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation = apiAccessor.generalTestDataProvider
            .generateOneCompanyInformationPerBackendOnlyFramework()
        apiAccessor.uploadCompanyAndFrameworkDataForMultipleFrameworks(
            mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation,
            1,
        )
        val distinctValues = apiAccessor.companyDataControllerApi.getAvailableCompanySearchFilters()
        assertTrue(
            distinctValues.sectors.intersect(
                mapOfAllBackendOnlyDataTypesToListOfOneCompanyInformation.map { it.value[0].sector }.toSet(),
            ).isEmpty(),
            "At least one sector of the frontend-excluded data sets appears in the distinct sector value list.",
        )
    }

    @Test
    fun `post a dummy company as teaser company and test if it is retrievable by company ID as unauthorized user`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(true)
        val getCompanyByIdResponse = apiAccessor.unauthorizedCompanyDataControllerApi.getCompanyById(
            uploadInfo.actualStoredCompany.companyId,
        )
        val expectedStoredTeaserCompany = StoredCompany(
            companyId = uploadInfo.actualStoredCompany.companyId,
            companyInformation = uploadInfo.inputCompanyInformation,
            dataRegisteredByDataland = emptyList(),
        )
        assertEquals(
            expectedStoredTeaserCompany,
            getCompanyByIdResponse,
            "The posted company does not equal the teaser company.",
        )
    }

    @Test
    fun `post a dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val exception = assertThrows<IllegalArgumentException> {
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
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
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
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithRandomIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
        val response = apiAccessor.companyDataControllerApi.postCompanyWithHttpInfo(testCompanyInformation)
            as ClientError

        assertEquals(
            400,
            response.statusCode,
            "The status code is ${response.statusCode} instead of the expected 400.",
        )
        assertTrue(
            response.body.toString().contains("Could not insert company as one company identifier is already used"),
            "The response message is not as expected.",
        )
    }

    @Test
    fun `check if the new companies search via name and ids endpoint works as expected`() {
        val testString = "unique-test-string-${UUID.randomUUID()}"
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        uploadCompaniesInReverseToExpectedOrder(testString)
        val sortedCompanyNames = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
            searchString = testString,
        ).map { it.companyName }
        assertEquals(
            listOf(testString, company2, "${testString}2", company5, company6, "3$testString", company9),
            sortedCompanyNames.filter { it != company8 && it != company3 },
        )
        assertEquals(
            listOf(company3, company2, "${testString}2", company5, company6, company8, company9),
            sortedCompanyNames.filter { it != "3$testString" && it != testString },
        )

        val otherCompanyNames = apiAccessor.companyDataControllerApi.getCompaniesBySearchString(
            searchString = "other_name",
        ).map { it.companyName }
        assertTrue(otherCompanyNames.contains(company8))
        assertFalse(otherCompanyNames.contains("Company 7"))
    }

    private fun uploadCompaniesInReverseToExpectedOrder(expectedSearchString: String) {
        uploadModifiedBaseCompany(company9, null, "3$expectedSearchString")
        uploadModifiedBaseCompany(company8, listOf("3$expectedSearchString", "other_name"), null)
        uploadModifiedBaseCompany("3$expectedSearchString", null, null)
        uploadModifiedBaseCompany(company6, null, "${expectedSearchString}2")
        uploadModifiedBaseCompany(company5, listOf("${expectedSearchString}2"), null)
        uploadModifiedBaseCompany("${expectedSearchString}2", null, null)
        uploadModifiedBaseCompany(company3, null, expectedSearchString)
        uploadModifiedBaseCompany(company2, listOf(expectedSearchString), null)
        uploadModifiedBaseCompany(expectedSearchString, null, null)
    }

    private fun uploadModifiedBaseCompany(name: String, alternativeNames: List<String>?, identifier: String?) {
        val companyInformation = baseCompanyInformation.copy(
            companyName = name,
            companyAlternativeNames = alternativeNames,
            identifiers = mapOf(
                IdentifierType.isin.value to listOf(identifier ?: UUID.randomUUID().toString()),
            ),
        )
        apiAccessor.companyDataControllerApi.postCompany(companyInformation)
    }
}
