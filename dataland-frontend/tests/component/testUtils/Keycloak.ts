import type Keycloak from 'keycloak-js';
import { type KeycloakInitOptions } from 'keycloak-js';

import { KEYCLOAK_ROLE_USER } from '@/utils/KeycloakRoles.ts';

export interface KeycloakMockConfiguration {
  userId?: string;
  roles?: Array<string>;
  idTokenParsed?: { picture: string };
  authenticated?: boolean;
}

/**
 * A function returning a minimal keycloak mock sufficient for tricking the dataland frontend.
 * @param config some configuration options that specify the user you want to imitate
 * @returns a mocked keycloak object sufficient to trick the dataland frontend
 */
export function minimalKeycloakMock(config: KeycloakMockConfiguration = {}): Keycloak {
  const mock = {
    token: 'mocked-token',
    authenticated: config.authenticated ?? true,
    idTokenParsed: config.idTokenParsed ?? {
      sub: config.userId ?? 'mock-user-id',
    },
    realmAccess: {
      roles: config.roles ?? [KEYCLOAK_ROLE_USER],
    },
    /*
      The updateToken method is invoked several times on the Keycloak object (e.g. implicitly in the ApiClients.ts).
      Therefore, a mock of the keycloak object also needs to provide this method.
      ESLint, however, does not recognize the usage of this function ==> ESlint-Disable
     */
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    updateToken(minValidity: number): Promise<boolean> {
      return Promise.resolve(true);
    },
    hasRealmRole(role: string): boolean {
      return this.realmAccess.roles.includes(role);
    },
    /*
      The init is required when developing locally against a remote backend. ESLint, however, does not recognize
      the usage of this function ==> ESlint-Disable
     */
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    init(initOptions: KeycloakInitOptions): Promise<boolean> {
      return Promise.resolve(true);
    },
  };
  return mock as Keycloak;
}
