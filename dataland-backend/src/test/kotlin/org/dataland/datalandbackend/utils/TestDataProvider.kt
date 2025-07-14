package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.LARGE_DECIMAL
import org.dataland.datalandbackend.services.QUALITY_STRING
import org.dataland.datalandbackend.services.TEST_ALIAS_1
import org.dataland.datalandbackend.services.TEST_ALIAS_2
import org.dataland.datalandbackend.services.TEST_CATEGORY
import org.dataland.datalandbackend.services.TEST_DATA_POINT_NAME
import org.dataland.datalandbackend.services.TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET
import org.dataland.datalandbackend.services.VALUE_STRING
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.time.Instant

class TestDataProvider(
    @Autowired var objectMapper: ObjectMapper,
) {
    private val jsonFile = File("./build/resources/test/CompanyInformationWithLksgData.json")
    private val testCompanyInformationWithLksgData =
        objectMapper.readValue(
            jsonFile,
            object : TypeReference<List<CompanyInformationWithData<LksgData>>>() {},
        )

    fun getCompanyInformation(requiredQuantity: Int): List<CompanyInformation> =
        testCompanyInformationWithLksgData
            .slice(
                0 until requiredQuantity,
            ).map { it.companyInformation }

    fun getCompanyInformationWithoutIdentifiers(requiredQuantity: Int): List<CompanyInformation> =
        getCompanyInformation(requiredQuantity)
            .map { it.copy(identifiers = IdentifierType.entries.associateWith { emptyList() }) }

    fun getLksgDataset(): LksgData = testCompanyInformationWithLksgData.first().t

    fun getEmptyStoredCompanyEntity(): StoredCompanyEntity =
        StoredCompanyEntity(
            "",
            "",
            null,
            null,
            null,
            "",
            null,
            "",
            "",
            mutableListOf(),
            null,
            mutableListOf(),
            "",
            false,
            null,
        )

    fun addCompanyAndReturnStorableDatasetForIt(
        companyAlterationManager: CompanyAlterationManager,
        frameworkName: String,
    ): StorableDataset {
        val companyInformation = getCompanyInformation(1).first()
        val companyId = companyAlterationManager.addCompany(companyInformation).companyId
        return StorableDataset(
            companyId,
            DataType(frameworkName),
            "USER_ID_OF_AN_UPLOADING_USER",
            Instant.now().toEpochMilli(),
            "",
            "someData",
        )
    }

    /**
     * Creates a test JSON with a data point that has only a quality field. The value field is null.
     */
    fun createTestJsonWithQualityNullValue(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>(TEST_CATEGORY, testField)

        val testPoint = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME, testPoint)

        testPoint.set<JsonNode>(VALUE_STRING, NullNode.instance)
        testPoint.put(QUALITY_STRING, QualityOptions.Audited.toString())

        return root
    }

    /**
     * Creates a test JSON with a data point that has both a non-null value and quality field
     */
    fun createTestJsonWithBothValueAndQuality(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>(TEST_CATEGORY, testField)

        val testPoint = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME, testPoint)

        testPoint.put(VALUE_STRING, "42")
        testPoint.put(QUALITY_STRING, QualityOptions.Reported.toString())

        return root
    }

    /**
     * Creates a test JSON with a large decimal value
     */
    fun createTestJsonWithLargeDecimal(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>(TEST_CATEGORY, testField)

        val testPoint = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME, testPoint)

        // Set both value and quality fields
        testPoint.put(VALUE_STRING, LARGE_DECIMAL)
        testPoint.put(QUALITY_STRING, QualityOptions.Reported.toString())

        return root
    }

    /**
     * Creates a test JSON with a data point that has both a value and quality field
     */
    fun createTestJsonWithTwoDataPoints(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>(TEST_CATEGORY, testField)

        val testPointA = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET, testPointA)

        testPointA.put(VALUE_STRING, "123")
        testPointA.put(QUALITY_STRING, QualityOptions.Reported.toString())

        val testPointB = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME, testPointB)

        testPointB.put(VALUE_STRING, "42")
        testPointB.put(QUALITY_STRING, QualityOptions.Reported.toString())

        return root
    }

    /**
     * Creates a test JSON with a data point that has a non primitive type
     */
    fun createTestJsonNonPrimitiveValue(): JsonNode {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>(TEST_CATEGORY, testField)

        val testPoint = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME, testPoint)

        val nonPrimitiveValue = objectMapper.createObjectNode()
        testPoint.set<JsonNode>(VALUE_STRING, nonPrimitiveValue)
        testPoint.put(QUALITY_STRING, QualityOptions.Reported.toString())
        nonPrimitiveValue.put("attribute1", 123)
        nonPrimitiveValue.put("attribute2", "test")

        return root
    }

    /**
     * Creates a test specification
     */
    fun createTestSpecification(): String {
        val root = objectMapper.createObjectNode()

        val testField = objectMapper.createObjectNode()
        root.set<JsonNode>(TEST_CATEGORY, testField)

        val testPoint1 = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME, testPoint1)

        testPoint1.put("id", "testId1")
        testPoint1.put("ref", "testRef1")
        testPoint1.put("aliasExport", TEST_ALIAS_1)
        // make sure that the data point that would be alphabetically first is not the first one in the specification
        // to check, that the order is indeed according to the specification
        val testPoint2 = objectMapper.createObjectNode()
        testField.set<JsonNode>(TEST_DATA_POINT_NAME_FIRST_IN_ALPHABET, testPoint2)

        testPoint2.put("id", "testId2")
        testPoint2.put("ref", "testRef2")
        testPoint2.put("aliasExport", TEST_ALIAS_2)

        return root.toString()
    }
}
