package org.dataland.datalandspecificationservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandspecificationservice.DatalandSpecificationService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * Test class for the SpecificationController.
 */
@SpringBootTest(
    classes = [DatalandSpecificationService::class],
    properties = ["dataland.specification-folder=res:/specifications-test"],
)
class SpecificationControllerTest(
    @Autowired val specificationController: SpecificationController,
    @Autowired val objectMapper: ObjectMapper,
) {
    @Test
    fun `retrieving a framework specification should return a DTO with correct refs and alias in schema`() {
        val response = specificationController.getFrameworkSpecification("test-framework")
        assert(response.statusCode.is2xxSuccessful)
        val schema = objectMapper.readTree(response.body!!.schema)
        assert(
            schema
                .path("test1")
                .path("test2")
                .path("test3")
                .path("ref")
                .textValue() ==
                "https://local-dev.dataland.com/specifications/data-point-types/test-datapoint-type",
        )
        assert(
            schema
                .path("test1")
                .path("test2")
                .path("test3")
                .path("aliasExport")
                .textValue() ==
                "alias",
        )
    }

    @Test
    fun `retrieving a data point specification should return a DTO with correct refs in usedBy`() {
        val response = specificationController.getDataPointTypeSpecification("test-datapoint-type")
        assert(response.statusCode.is2xxSuccessful)
        val body = response.body!!
        assert(body.usedBy[0].ref == "https://local-dev.dataland.com/specifications/frameworks/test-framework")
    }

    @Test
    fun `retrieving a data point type specification should return a DTO with correct refs in usedBy`() {
        val response = specificationController.getDataPointBaseType("test-datapoint-base-type")
        assert(response.statusCode.is2xxSuccessful)
        val body = response.body!!
        assert(body.usedBy[0].ref == "https://local-dev.dataland.com/specifications/data-point-types/test-datapoint-type")
    }

    @Test
    fun `retrieving a non existing framework specification should return 404`() {
        assertThrows<ResourceNotFoundApiException> {
            specificationController.getFrameworkSpecification("non-existing-framework")
        }
    }

    @Test
    fun `retrieving a non existing data point specification should return 404`() {
        assertThrows<ResourceNotFoundApiException> {
            specificationController.getDataPointTypeSpecification("non-existing-datapoint")
        }
    }

    @Test
    fun `retrieving a non existing data point type specification should return 404`() {
        assertThrows<ResourceNotFoundApiException> {
            specificationController.getDataPointBaseType("non-existing-datapoint-type")
        }
    }

    @Test
    fun `returning schema for known base type`() {
        val frameworkSpecificationId = "test-framework"
        val response = specificationController.getResolvedFrameworkSpecification(frameworkSpecificationId)
        assert(response.statusCode.is2xxSuccessful)
        assert(response.body?.resolvedSchema.toString() == "{test1={test2={test3={\"testing\":\"Boolean\"}}}}")
    }
}
