import {useQuery} from "@tanstack/vue-query";
import {queryKeys} from "@/queries/queryKeys.ts";
import {computed, type Ref, inject} from "vue";
import Keycloak from "keycloak-js";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import  {hasCompanyAtLeastOneCompanyOwner} from "@/utils/CompanyRolesUtils.ts";
import {checkIfUserHasRole} from "@/utils/KeycloakUtils.ts";
import {KEYCLOAK_ROLE_ADMIN} from "@/utils/KeycloakRoles.ts";


export function useUserAdminQuery() {
    const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!getKeycloakPromise) throw new Error("Keycloak not provided!");

    const query = useQuery<boolean>({
        queryKey: queryKeys.userAdmin(),
        queryFn: () => checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise),
        select: (data) => data ?? false,
        staleTime: 1000*60*60*24,
    });
    const isAdmin = computed(() => query.data.value ?? false);

    return {
        ... query,
        isAdmin
    }
}