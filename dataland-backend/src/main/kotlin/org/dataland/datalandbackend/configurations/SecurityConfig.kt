package org.dataland.datalandbackend.configurations

import org.springframework.stereotype.Component

/**
 * This class contains all security configurations for the backend to secure endpoints with keycloak.
 */
class SecurityConfig {
    /**
     * The Role Container is a component which maps actual roles from the Dataland keycloak service to the role
     * definitions which are used in the Dataland backend code to decouple the role namings in the keycloak service
     * from the ones in backend.
     */
    @Component("RoleContainer")
    object RoleContainer {
        const val DATA_READER = "ROLE_USER"
        const val DATA_UPLOADER = "ROLE_ADMIN"
    }
}
