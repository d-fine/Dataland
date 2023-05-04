import Keycloak, { KeycloakPromise } from "keycloak-js";

export interface KeycloakMockConfiguration {
  userId?: string;
  roles?: Array<string>;
}

/**
 * A function returning a minmal keycloak mock sufficient for tricking the dataland frontend.
 * @param config some configuration options that specify the user you want to imitate
 * @returns a mocked keycloak object sufficient to trick the dataland frontend
 */
export function minimalKeycloakMock(config: KeycloakMockConfiguration): Keycloak {
  const mock = {
    token: "mocked-token",
    authenticated: true,
    idTokenParsed: {
      sub: config.userId ?? "mock-user-id",
    },
    realmAccess: {
      roles: config.roles ?? ["ROLE_USER"],
    },
    /*
      The updateToken method is invoked several times on the Keycloak object (e.g. implicitly in the ApiClients.ts).
      Therefore, a mock of the keycloak object also needs to provide this method.
      ESLint, however, does not recognize the usage of this function ==> ESlint-Disable
     */
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    updateToken(minValidity: number): KeycloakPromise<boolean, boolean> {
      return Promise.resolve(true) as KeycloakPromise<boolean, boolean>;
    },
  };
  return mock as Keycloak;
}
