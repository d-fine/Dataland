import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys';
import { computed, type Ref } from 'vue';
import {ApiClientProvider} from "@/services/ApiClients.ts";
import {useApiClient} from "@/utils/api/useApiClient.ts";

export function useCompanyCreditsQuery(
    companyId: Ref<string> | string
) {
    const id = computed(() =>
        typeof companyId === 'string' ? companyId : companyId.value
    );
    const apiClientProvider : ApiClientProvider = useApiClient();

    return useQuery<number>({
        queryKey: computed(() => queryKeys.creditsBalance(id.value)),
        enabled: computed(() => !!id.value),
        queryFn: async () => {
            const response =
                await apiClientProvider.apiClients.creditsController.getBalance(id.value);
            return response.data as number;
        },
        initialData: 0,
    });

}
