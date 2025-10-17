package org.dataland.datalandbackend.validator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.controller.CompanyDataController
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyBaseManager
import org.dataland.datalandbackend.services.CompanyIdentifierManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.datalandbackendutils.utils.JsonComparator.compareJson
import org.dataland.datalandbackendutils.utils.JsonComparator.compareJsonStrings
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.DataPointBaseTypeSpecification
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Suppress("LongParameterList")
class DataPointSpecificationExampleValidationTest
    @Autowired
    constructor(
        private val dataPointValidator: DataPointValidator,
        private val objectMapper: ObjectMapper,
        private val dataPointManager: DataPointManager,
        private val companyAlterationManager: CompanyAlterationManager,
        private val companyQueryManager: CompanyQueryManager,
        private val companyIdentifierManager: CompanyIdentifierManager,
        private val companyBaseManager: CompanyBaseManager,
        private val dataPointUtils: DataPointUtils,
    ) {
        lateinit var companyController: CompanyDataController
        private lateinit var companyIdOfPostedCompany: String
        private var dummyUuid = "00000000-0000-0000-0000-000000000000"

        @BeforeAll
        fun seedCompanyOnce() {
            companyController =
                CompanyDataController(
                    companyAlterationManager,
                    companyQueryManager,
                    companyIdentifierManager,
                    companyBaseManager,
                    dataPointUtils,
                )

            companyIdOfPostedCompany = postCompany(companyWithTestLei)
        }

        companion object {
            private const val DATA_POINT_BASE_TYPE_DIRECTORY =
                "../dataland-specification-service/src/main/resources/specifications/dataPointBaseTypes"

            @JvmStatic
            fun dataPointBaseTypeTestProvider(): List<Arguments> =
                File(DATA_POINT_BASE_TYPE_DIRECTORY)
                    .listFiles()!!
                    .filter {
                        it.isFile
                    }.map {
                        val dataPointBaseTypeId = it.nameWithoutExtension
                        Arguments.of(dataPointBaseTypeId, it)
                    }

            private const val TEST_LEI = "testLei"

            val companyWithTestLei =
                CompanyInformation(
                    companyName = "Test Company",
                    companyAlternativeNames = null,
                    companyContactDetails = null,
                    companyLegalForm = null,
                    countryCode = "DE",
                    headquarters = "Berlin",
                    headquartersPostalCode = "8",
                    fiscalYearEnd = null,
                    reportingPeriodShift = null,
                    sector = null,
                    sectorCodeWz = null,
                    website = null,
                    isTeaserCompany = null,
                    identifiers = mapOf(IdentifierType.Lei to listOf(TEST_LEI)),
                    parentCompanyLei = null,
                )
        }

        @MockitoBean
        private lateinit var ignored: DocumentControllerApi

        @ParameterizedTest(name = "Ensure the datapoint example {0} matches the specification")
        @MethodSource("dataPointBaseTypeTestProvider")
        fun `ensure the datapoint example matches the specification`(
            ignored: String,
            file: File,
        ) {
            val fileContents = objectMapper.readTree(file)
            val validatedBy = fileContents["validatedBy"].asText()
            val example = fileContents["example"].toString()
            assertDoesNotThrow {
                dataPointValidator.validateConsistency(example, validatedBy, "correlationId")
            }
        }

        @ParameterizedTest(name = "Ensure the datapoint example {0} matches the schema structure and class")
        @MethodSource("dataPointBaseTypeTestProvider")
        fun `ensure the datapoint example matches the schema structure and class`(
            ignored: String,
            file: File,
        ) {
            val fileContents = objectMapper.readTree(file)
            val schema = fileContents["schema"]
            val example = fileContents["example"]
            val validatedBy = fileContents["validatedBy"].asText()

            requireNotNull(schema) { "Schema is missing in ${file.name}" }
            requireNotNull(example) { "Example is missing in ${file.name}" }

            val generated = buildJsonFromSchemaWithExample(schema, example, objectMapper)

            assertDoesNotThrow {
                dataPointValidator.validateConsistency(
                    objectMapper.writeValueAsString(generated),
                    validatedBy,
                    "correlationId",
                )
            }

            val jsonDifferences =
                compareJson(
                    schema,
                    example,
                    JsonComparator.JsonComparisonOptions(ignoreValues = true, fullyNullObjectsAreEqualToNull = false),
                ).filter { !it.path.matches(Regex(".+\\[[0-9][1-9]*\\]$")) }
            assertEquals(emptyList<JsonComparator.JsonDiff>(), jsonDifferences)
        }

        @MockitoBean
        private lateinit var companyRoleChecker: CompanyRoleChecker

        @MockitoBean
        private lateinit var specificationClient: SpecificationControllerApi

        @Suppress("unused")
        @MockitoBean
        private lateinit var messageQueuePublications: org.dataland.datalandbackend.services.MessageQueuePublications

        @ParameterizedTest(name = "Ensure datapoints are retrieved as they are uploaded")
        @MethodSource("dataPointBaseTypeTestProvider")
        fun `ensure datapoints are retrieved as they are uploaded`(
            dataPointBaseTypeId: String,
            file: File,
        ) {
            val fileContents = objectMapper.readTree(file)
            val validatedByRef = fileContents["validatedBy"].asText()

            doReturn(true).whenever(companyRoleChecker).canUserBypassQa(any())
            mockSecurityContext()

            val uploadedDataPoint =
                UploadedDataPoint(
                    dataPoint = objectMapper.writeValueAsString(fileContents["example"]),
                    dataPointType = dataPointBaseTypeId,
                    companyId = companyIdOfPostedCompany,
                    reportingPeriod = "2025",
                )

            val dataPointTypeMock = mock<IdWithRef> { on { id } doReturn "dummy" }
            val dataPointTypeSpecificationMock =
                mock<DataPointTypeSpecification> {
                    on { dataPointBaseType } doReturn dataPointTypeMock
                    on { constraints } doReturn null
                }
            doReturn(dataPointTypeSpecificationMock).whenever(specificationClient).getDataPointTypeSpecification(any())

            val dataPointBaseTypeSpecificationMock =
                mock<DataPointBaseTypeSpecification> {
                    on { validatedBy } doReturn validatedByRef
                }
            doReturn(dataPointBaseTypeSpecificationMock).whenever(specificationClient).getDataPointBaseType(any())

            val response = dataPointManager.processDataPoint(uploadedDataPoint, dummyUuid, bypassQa = true, dummyUuid)
            val downloadedDataPoint = dataPointManager.retrieveDataPoint(response.dataPointId, "correlationId")

            assertEquals(
                emptyList<JsonComparator.JsonDiff>(),
                compareJsonStrings(uploadedDataPoint.dataPoint, downloadedDataPoint.dataPoint),
            )
        }

        fun postCompany(company: CompanyInformation = companyWithTestLei): String =
            companyController
                .postCompany(
                    company,
                ).body!!
                .companyId

        private fun mockSecurityContext() {
            val mockAuthentication =
                AuthenticationMock.mockJwtAuthentication(
                    "mocked_uploader",
                    "dummy-id",
                    setOf(DatalandRealmRole.ROLE_ADMIN),
                )
            val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
            `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
            SecurityContextHolder.setContext(mockSecurityContext)
        }

        fun buildJsonFromSchemaWithExample(
            schema: JsonNode,
            example: JsonNode,
            objectMapper: ObjectMapper,
        ): JsonNode =
            when {
                schema.isObject -> {
                    val objectNode = objectMapper.createObjectNode()
                    schema.fieldNames().forEach { field ->
                        val schemaField = schema[field]
                        val exampleField = example.get(field)
                        objectNode.set<JsonNode>(field, buildJsonFromSchemaWithExample(schemaField, exampleField, objectMapper))
                    }
                    objectNode
                }
                schema.isArray -> {
                    val array = objectMapper.createArrayNode()
                    val schemaElement = schema.firstOrNull()
                    val exampleArray = example
                    if (exampleArray.isArray && schemaElement != null) {
                        exampleArray.forEach { exampleElement ->
                            array.add(buildJsonFromSchemaWithExample(schemaElement, exampleElement, objectMapper))
                        }
                    }
                    array
                }
                schema.isTextual -> {
                    example
                }
                else -> objectMapper.nullNode()
            }
    }
