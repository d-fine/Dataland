package org.dataland.datalandbackend.validator

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.io.File

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class DataPointSpecificationExampleValidationTest
    @Autowired
    constructor(
        private val dataPointValidator: DataPointValidator,
        private val objectMapper: ObjectMapper,
    ) {
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
    }
