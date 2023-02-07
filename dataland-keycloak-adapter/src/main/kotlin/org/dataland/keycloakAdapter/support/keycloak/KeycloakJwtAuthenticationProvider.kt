package org.dataland.keycloakAdapter.support.keycloak

import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken

/**
 * This provider supports login via JWTs provided as bearer tokens.
 * Generates a DatalandJwtAuthentication upon successful validation of the JWT.
 */
class KeycloakJwtAuthenticationProvider(val jwtDecoder: JwtDecoder) : AuthenticationProvider {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun authenticate(authentication: Authentication): DatalandJwtAuthentication {
        val bearerToken = authentication as BearerTokenAuthenticationToken
        logger.trace("Received request for authentication with bearer token ${bearerToken.token}")
        val jwt = decodeAndValidateJwt(bearerToken)
        logger.trace("JWT validated: ${jwt}. Continuing authentication")
        val jwtAuthentication = DatalandJwtAuthentication(jwt)
        jwtAuthentication.isAuthenticated = true
        return jwtAuthentication
    }

    private fun decodeAndValidateJwt(bearer: BearerTokenAuthenticationToken): Jwt {
        return try {
            jwtDecoder.decode(bearer.token)
        } catch (ex: BadJwtException) {
            logger.trace("Authentication failed as the JWT was invalid", ex)
            throw InvalidBearerTokenException(ex.message, ex)
        } catch (ex: JwtException) {
            logger.trace("Internal JWT handling exception", ex)
            throw AuthenticationServiceException(ex.message, ex)
        }
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return BearerTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
