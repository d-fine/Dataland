import {useQuery} from "@tanstack/vue-query";
import {queryKeys} from "@/queries/queryKeys.ts";
import {computed, type Ref, inject} from "vue";
import Keycloak from "keycloak-js";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import  {hasCompanyAtLeastOneCompanyOwner} from "@/utils/CompanyRolesUtils.ts";


export function useHasCompanyOwnerQuery(
    companyId: Ref<string> | string) {
    const id  = computed(() => typeof companyId === 'string' ? companyId : companyId.value);

    const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!getKeycloakPromise) throw new Error("Keycloak not provided!");
    const apiClientProvider = new ApiClientProvider(getKeycloakPromise());

    const key = queryKeys.hasCompanyOwnership(id.value);

    const query = useQuery({
        queryKey: key,
        enabled: computed(() => !!id.value),
        queryFn: async () => {
            const response = await hasCompanyAtLeastOneCompanyOwner(id.value, getKeycloakPromise);
            return response as boolean;
        }
    })

    const hasCompanyOwner = computed(() => query.data.value ?? false);
    return {
        ... query, query,
        hasCompanyOwner
    }
}