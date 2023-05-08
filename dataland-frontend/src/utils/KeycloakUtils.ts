import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";

/**
 * Asserts that the provided getter-function to get a Keycloak-promise is defined, then executes that getter-function
 * and returns the Keycloak-promise
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-Ppomise
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

/**
 * Derives the roles from the resolved Keycloak-promise of a logged in user and checks if the role for uploading data
 * is included.
 * @param keycloakPromiseGetter the getter-function which returns a Keycloak-promise
 * @returns a promise, which resolves to a boolean
 */
export async function checkIfUserHasUploaderRights(keycloakPromiseGetter?: () => Promise<Keycloak>): Promise<boolean> {
  if (keycloakPromiseGetter) {
    const roles = await getKeycloakRolesForUser(keycloakPromiseGetter);
    if (roles) {
      return roles.includes("ROLE_UPLOADER");
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
export function loginAndRedirectToSearchPage(keycloak: Keycloak): void {
  const baseUrl = window.location.origin;
  const url = keycloak.createLoginUrl({ redirectUri: `${baseUrl}/companies` });
  location.assign(url);
}
