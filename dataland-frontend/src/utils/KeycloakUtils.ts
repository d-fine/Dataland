import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";

export async function waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter: () => Promise<Keycloak>) {
  const keycloakPromiseGetterAsserted: () => Promise<Keycloak> = assertDefined(keycloakPromiseGetter);
  return await keycloakPromiseGetterAsserted();
}

export async function getKeycloakRolesForUser(keycloakPromiseGetter: () => Promise<Keycloak>) {
  const resolvedKeycloakPromise = await waitForAndReturnResolvedKeycloakPromise(keycloakPromiseGetter);
  if (resolvedKeycloakPromise.realmAccess) {
    const roles = resolvedKeycloakPromise.realmAccess.roles;
    return roles;
  }
}

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
