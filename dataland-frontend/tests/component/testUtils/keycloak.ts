import Keycloak from "keycloak-js";

export interface KeycloakMockConfiguration {
  userId?: string;
  roles?: Array<string>;
}

/**
 * A function returning a minmal keycloak mock sufficient for tricking the dataland frontend.
 *
 * @param config some configuration options that specify the user you want to imitate
 * @returns a mocked keycloak object sufficient to trick the dataland frontend
 */
export function minimalKeycloakMock(config: KeycloakMockConfiguration): Keycloak {
  const mock = {
    token: "mocked-token",
    authenticated: true,
    idTokenParsed: {
      sub: config.userId || "mock-user-id",
    },
    realmAccess: {
      roles: config.roles || ["ROLE_USER"],
    },
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    updateToken(minValidity: number): void {
      return;
    },
  };
  return mock as Keycloak;
}
