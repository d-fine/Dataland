import Keycloak, {KeycloakError, KeycloakPromise} from "keycloak-js";

const a: string = "aaa"

export function getInjectedKeycloakObjectsForTest(): any{
    return {
        getKeycloakInitPromise() {
            return a;
        },
        keycloak_init: a,
    }
}