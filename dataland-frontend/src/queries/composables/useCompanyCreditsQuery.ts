import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys';
import { computed, type Ref } from 'vue';
import type { ApiClientProvider } from '@/services/ApiClients';

export function useCompanyCreditsQuery(
    companyId: Ref<string> | string,
    apiClientProvider: ApiClientProvider
) {
    const id = computed(() =>
        typeof companyId === 'string' ? companyId : companyId.value
    );

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
