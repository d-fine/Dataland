import {useQuery} from "@tanstack/vue-query";
import {queryKeys} from "@/queries/queryKeys.ts";
import {computed, type Ref, inject, unref} from "vue";
import Keycloak from "keycloak-js";
import  {hasCompanyAtLeastOneCompanyOwner} from "@/utils/CompanyRolesUtils.ts";


export function useHasCompanyOwnerQuery(
    companyId: Ref<string> | string) {
    const id = computed(() => unref(companyId));

    const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!getKeycloakPromise) throw new Error("Keycloak not provided!");

    return useQuery<boolean>({
        queryKey: computed (() => queryKeys.hasCompanyOwnership(id.value)),
        enabled: computed(() => !!id.value),
        queryFn: async () => {
            const response = await hasCompanyAtLeastOneCompanyOwner(id.value, getKeycloakPromise);
            return response as boolean;
        },
        initialData: false,
    })

}