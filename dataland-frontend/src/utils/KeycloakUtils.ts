import { assertDefined } from '@/utils/TypeScriptUtils';
import Keycloak from '../../node_modules/keycloak-js/lib/keycloak.js';

/**
 * Asserts that the provided getter-function to get a Keycloak-promise is defined, then executes that getter-function
 * and returns the Keycloak-promise
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Promise
 * @returns the Keycloak-promise returned by the getter-function
 */
export async function waitForAndReturnResolvedKeycloakPromise(
  keycloakPromiseGetter: () => Promise<Keycloak>
): Promise<Keycloak> {
  const keycloakPromiseGetterAsserted: () => Promise<Keycloak> = assertDefined(keycloakPromiseGetter);
  return keycloakPromiseGetterAsserted();
}

/**
 * Derives the roles from the resolved Keycloak-promise of a logged in user and returns them.
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-promise
 * @returns a promise, which resolves to an array containing the roles of the user as strings
 */
export async function getKeycloakRolesForUser(keycloakPromiseGetter: () => Promise<Keycloak>): Promise<Array<string>> {
  const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
  if (resolvedKeycloakPromise.realmAccess) {
    return resolvedKeycloakPromise.realmAccess.roles;
  } else return [];
}

export const KEYCLOAK_ROLE_USER = 'ROLE_USER';
export const KEYCLOAK_ROLE_UPLOADER = 'ROLE_UPLOADER';
export const KEYCLOAK_ROLE_REVIEWER = 'ROLE_REVIEWER';
export const KEYCLOAK_ROLE_ADMIN = 'ROLE_ADMIN';
export const KEYCLOAK_ROLE_PREMIUM_USER = 'ROLE_PREMIUM_USER';
export const KEYCLOAK_ROLES = [
  KEYCLOAK_ROLE_UPLOADER,
  KEYCLOAK_ROLE_USER,
  KEYCLOAK_ROLE_ADMIN,
  KEYCLOAK_ROLE_REVIEWER,
  KEYCLOAK_ROLE_PREMIUM_USER,
];
/**
 * Derives the roles from the resolved Keycloak-promise of a logged in user
 * and checks if the provided role is included.
 * @param expectedKeycloakRole the keycloak user role to test for
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-promise
 * @returns a promise, which resolves to a boolean
 */
export async function checkIfUserHasRole(
  expectedKeycloakRole: string,
  keycloakPromiseGetter?: () => Promise<Keycloak>
): Promise<boolean> {
  if (keycloakPromiseGetter) {
    const rolesOfUser = await getKeycloakRolesForUser(keycloakPromiseGetter);
    if (rolesOfUser) {
      return rolesOfUser.includes(expectedKeycloakRole);
    } else {
      return false;
    }
  } else return false;
}

/**
 * Logs the user out and redirects her/him to the base url concatenated with the passed redirectPath.
 * @param keycloak is the keycloak adaptor used to do the logout
 * @param additionToBasePath is the addition to the base url to result in the final url that the user shall be
 * redirected to
 */
export function logoutAndRedirectToUri(keycloak: Keycloak, additionToBasePath: string): void {
  const baseUrl = window.location.origin;
  const url = keycloak.createLogoutUrl({ redirectUri: `${baseUrl}${additionToBasePath}` });
  location.assign(url);
}

/**
 * Logs the user in and redirects her/him to the Dataland companies search page.
 * @param keycloak is the keycloak adaptor used to do the login
 */
export async function loginAndRedirectToSearchPage(keycloak: Keycloak): Promise<void> {
  const baseUrl = window.location.origin;
  const url = await keycloak.createLoginUrl({ redirectUri: `${baseUrl}/companies` });
  location.assign(url);
}

/**
 * Registers and logs the user in and redirects her/him to the Dataland companies search page.
 * @param keycloak is the keycloak adaptor used to do the login
 */
export async function registerAndRedirectToSearchPage(keycloak: Keycloak): Promise<void> {
  const baseUrl = window.location.origin;
  const url = await keycloak.createRegisterUrl({ redirectUri: `${baseUrl}/companies` });
  location.assign(url);
}

/**
 * Gets the user id
 * @param getKeycloakPromise the keycloak promise
 * @returns the user id as string or undefined
 */
export async function getUserId(getKeycloakPromise: () => Promise<Keycloak>): Promise<string | undefined> {
  const parsedIdToken = (await getKeycloakPromise()).idTokenParsed;
  if (parsedIdToken) {
    return parsedIdToken.sub;
  } else {
    return undefined;
  }
}
