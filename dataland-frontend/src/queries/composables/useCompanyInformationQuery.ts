// useCompanyInformationQuery.ts
import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys';
import { computed, type Ref } from 'vue';
import type { CompanyInformation } from '@clients/backend';
import type { ApiClientProvider } from '@/services/ApiClients';

export function useCompanyInformationQuery(
    companyId: Ref<string> | string,
    apiClientProvider: ApiClientProvider
) {
    const id = computed(() =>
        typeof companyId === 'string' ? companyId : companyId.value
    );

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
