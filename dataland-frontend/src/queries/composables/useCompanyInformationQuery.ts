// useCompanyInformationQuery.ts
import { useQuery } from '@tanstack/vue-query';
import { queryKeys } from '@/queries/queryKeys';
import {computed, type Ref} from 'vue';
import type { CompanyInformation } from '@clients/backend';
import {ApiClientProvider} from "@/services/ApiClients.ts";
import {useApiClient} from "@/utils/api/useApiClient.ts";

export function useCompanyInformationQuery(
    companyId: Ref<string> | string
) {
    const id = computed(() =>
        typeof companyId === 'string' ? companyId : companyId.value
    );

    const apiClientProvider : ApiClientProvider = useApiClient();

    return useQuery<CompanyInformation>({
        queryKey: computed(() => queryKeys.companyInformation(id.value)),
        enabled: computed(() => !!id.value),
        queryFn: async () => {
            const response =
                await apiClientProvider.backendClients.companyDataController.getCompanyInfo(
                    id.value
                );
            return response.data as CompanyInformation;
        },
    });


}
