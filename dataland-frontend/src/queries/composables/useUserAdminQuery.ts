import {useQuery} from "@tanstack/vue-query";
import {queryKeys} from "@/queries/queryKeys.ts";
import { inject} from "vue";
import Keycloak from "keycloak-js";
import {checkIfUserHasRole} from "@/utils/KeycloakUtils.ts";
import {KEYCLOAK_ROLE_ADMIN} from "@/utils/KeycloakRoles.ts";


export function useUserAdminQuery() {
    const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!getKeycloakPromise) throw new Error("Keycloak not provided!");

    return useQuery<boolean>({
        queryKey: queryKeys.userAdmin(),
        queryFn: () => checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise)
        ,
        initialData: false,
        staleTime: 1000*60*60*24, // 1 day, because user roles don't change often and is reloaded per page load anyway
    });

}