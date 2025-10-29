package org.dataland.e2etests.tests.communityManager

import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InheritedRolesControllerTest {
    private val apiAccessor = ApiAccessor()
    private val inheritedRolesControllerApi = apiAccessor.inheritedRolesControllerApi

    @ParameterizedTest
    @EnumSource(TechnicalUser::class)
    fun `verify that only Dataland admins can retrieve inherited roles`(user: TechnicalUser) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(user)

        if (user == TechnicalUser.Admin) {
            assertDoesNotThrow { inheritedRolesControllerApi.getInheritedRoles(UUID.randomUUID().toString()) }
        } else {
            assertThrows<ClientException> {
                inheritedRolesControllerApi.getInheritedRoles(UUID.randomUUID().toString())
            }
        }
    }
}
