// useCompanyInformationQuery.ts
import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys';
import {computed, inject, type Ref} from 'vue';
import type { CompanyInformation } from '@clients/backend';
import Keycloak from 'keycloak-js';
import {ApiClientProvider} from "@/services/ApiClients.ts";

export function useCompanyInformationQuery(
    companyId: Ref<string> | string
) {
    const id = computed(() =>
        typeof companyId === 'string' ? companyId : companyId.value
    );

    const keyCloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!keyCloakPromise) throw new Error("Keycloak not provided!");
    const apiClientProvider = new ApiClientProvider(keyCloakPromise());

    const key = computed(() => queryKeys.companyInformation(id.value));

    const query = useQuery<CompanyInformation>({
        queryKey: key,
        enabled: computed(() => !!id.value),
        queryFn: async () => {
            const response =
                await apiClientProvider.backendClients.companyDataController.getCompanyInfo(
                    id.value
                );
            return response.data as CompanyInformation;
        },
    });

    const companyInformation = computed<CompanyInformation | null>(
        () => query.data.value ?? null
    );

    return {
        ...query,
        companyInformation,
    };
}
