package org.dataland.datasourcingservice.integrationTests

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.model.request.RequestSearchFilter
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
@WithMockUser(username = "data_admin", roles = ["ADMIN"])
class UUIDValidationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        private const val NON_UUID_STRING = "not-a-uuid"
        private const val SEARCH_URL_STRING = "/requests/search"
        private const val COUNT_URL_STRING = "/requests/count"
    }

    private val objectMapper = jacksonObjectMapper()

    private fun createRequestSearchFilter(
        companyId: String?,
        userId: String?,
    ): RequestSearchFilter<String> =
        RequestSearchFilter<String>(
            companyId = companyId,
            userId = userId,
        )

    private fun postWithFilterAndExpectNotFound(
        urlString: String,
        companyId: String?,
        userId: String?,
    ) = mockMvc
        .perform(
            post(urlString)
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        createRequestSearchFilter(
                            companyId = companyId,
                            userId = userId,
                        ),
                    ),
                ),
        ).andExpect(status().isNotFound)

    @ParameterizedTest
    @CsvSource(
        value = [
            "$SEARCH_URL_STRING, $NON_UUID_STRING, null",
            "$SEARCH_URL_STRING, null, $NON_UUID_STRING",
            "$COUNT_URL_STRING, $NON_UUID_STRING, null",
            "$COUNT_URL_STRING, null, $NON_UUID_STRING",
        ],
        nullValues = ["null"],
    )
    fun `posting a query with an ID that is not a UUID leads to the appropriate error`(
        urlString: String,
        companyId: String?,
        userId: String?,
    ) {
        postWithFilterAndExpectNotFound(
            urlString = urlString,
            companyId = companyId,
            userId = userId,
        )
    }
}
