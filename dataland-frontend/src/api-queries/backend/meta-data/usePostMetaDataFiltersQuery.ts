import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { metaDataKeys } from '@/api-queries/backend/meta-data/metaDataKeys.ts';
import type { DataMetaInformation, DataMetaInformationSearchFilter } from '@clients/backend';

/**
 * Vue Query hook that fetches the meta data information based on filters.
 *
 * Uses POST /meta-data/search.
 *
 * @param filters - Reactive array of request search filters.
 * @param options - Additional query options (e.g. enabled).
 * @returns Query result containing the meta data of the matching datasets.
 */
export function usePostMetaDataFiltersQuery(
  filters: Readonly<Ref<DataMetaInformationSearchFilter[]>>,
  options?: Omit<UseQueryOptions<DataMetaInformation[], Error>, 'queryKey' | 'queryFn'>
): UseQueryReturnType<DataMetaInformation[], Error> {
  const apiClientProvider = useApiClient();

  const queryKey = computed(() => metaDataKeys.search(filters.value));

  return useQuery<DataMetaInformation[], Error>({
    queryKey,
    queryFn: async () => {
      const response = await apiClientProvider.backendClients.metaDataController.postListOfDataMetaInfoFilters(
        filters.value
      );

      return response.data;
    },
    ...options,
  });
}
