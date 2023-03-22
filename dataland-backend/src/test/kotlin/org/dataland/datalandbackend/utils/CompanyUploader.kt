package org.dataland.datalandbackend.utils
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CompanyUploader {

    private fun getMockUploaderAuthentication(): Authentication {
        return AuthenticationMock.mockJwtAuthentication(
            "mocked_uploader",
            "mocked_uploader_id",
            setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER),
        )
    }

    internal fun uploadCompany(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        companyInformation: CompanyInformation,
    ): StoredCompany {
        val mockAuthentication = getMockUploaderAuthentication()
        val request = mockMvc.perform(
            MockMvcRequestBuilders.post("/companies")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(companyInformation))
                .with(authentication(mockAuthentication)),
        )
            .andExpectAll(
                MockMvcResultMatchers.status().isOk,
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            ).andReturn()
        return objectMapper.readValue(
            request.response.contentAsString,
            object : TypeReference<StoredCompany>() {},
        )
    }
}
