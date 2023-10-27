package org.dataland.datalandcommunitymanager

class CommunityManagerTest {

/* TODO
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
    }*/
}
