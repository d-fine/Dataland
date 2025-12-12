import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys';
import { computed, type Ref } from 'vue';
import {inject} from "vue";
import Keycloak from "keycloak-js";
import {ApiClientProvider} from "@/services/ApiClients.ts";

export function useCompanyCreditsQuery(
    companyId: Ref<string> | string
) {
    const id = computed(() =>
        typeof companyId === 'string' ? companyId : companyId.value
    );

    const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
    if (!getKeycloakPromise) throw new Error("Keycloak not provided!");
    const apiClientProvider = new ApiClientProvider(getKeycloakPromise());

    const key = computed(() => queryKeys.creditsBalance(id.value));

    const query = useQuery<number>({
        queryKey: key,
        enabled: computed(() => !!id.value),
        queryFn: async () => {
            const response =
                await apiClientProvider.apiClients.creditsController.getBalance(id.value);
            return response.data as number;
        },
    });

    const creditsBalance = computed(() => query.data.value ?? 0);

    return {
        ...query,
        creditsBalance,
    };
}
