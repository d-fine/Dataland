package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class InheritedRolesControllerMockMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private val mockSecurityContext = mock<SecurityContext>()

    companion object {
        private val adminUserId = UUID.randomUUID().toString()
        private val nonAdminUserId = UUID.randomUUID().toString()
        private val userIds = listOf(adminUserId, nonAdminUserId)

        private val dummyAdminAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                username = "DATA_ADMIN",
                userId = adminUserId,
                roles = setOf(DatalandRealmRole.ROLE_ADMIN),
            )
        private val dummyNonAdminAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                username = "DATA_READER",
                userId = nonAdminUserId,
                roles = setOf(DatalandRealmRole.ROLE_USER),
            )
        val dummyAuthentications = listOf(dummyAdminAuthentication, dummyNonAdminAuthentication)

        @JvmStatic
        @Suppress("UnusedPrivateMember") // detekt wrongly thinks this function is unused
        private fun testParametersSource(): List<Arguments> =
            dummyAuthentications.flatMap { dummyAuthentication ->
                userIds.map { userId -> Arguments.of(dummyAuthentication, userId) }
            }
    }

    @BeforeEach
    fun setup() {
        reset(mockSecurityContext)
    }

    @ParameterizedTest
    @MethodSource("testParametersSource")
    fun `verify that access rights for retrieving inherited roles are as expected`(
        dummyAuthentication: DatalandJwtAuthentication,
        userId: String,
    ) {
        doReturn(dummyAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)

        val userIsRequestingForOwnId = dummyAuthentication.userId == userId

        mockMvc
            .perform(
                get("/inherited-roles/$userId")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(
                if (userIsRequestingForOwnId || dummyAuthentication.userId == adminUserId) {
                    status().isOk()
                } else {
                    status().isForbidden()
                },
            )
    }
}
