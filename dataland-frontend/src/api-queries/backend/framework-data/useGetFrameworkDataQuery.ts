import { computed, unref, type MaybeRef } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { DataTypeEnum } from '@clients/backend';
import { useApiClient } from '@/utils/useApiClient';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils';
import { frameworkDataKeys } from './frameworkDataKeys';
import { type CompanyAssociatedData } from '@/api-models/CompanyAssociatedData';

// Core fetcher (no Vue here, easy to test)
/**
 * Fetch framework-specific data by resolving the correct backend API for the
 * provided framework and returning the data for the given id.
 *
 * @param {DataTypeEnum} framework - Framework / data type used to select the API.
 * @param {string} dataId - Identifier of the data to fetch.
 * @param {ReturnType<typeof useApiClient>} apiClientProvider - Provider for backend API clients.
 * @returns {Promise<CompanyAssociatedData<object>>} Resolves with the fetched company-associated data.
 * @throws {Error} If no API implementation exists for the provided framework or the request fails.
 */
async function fetchFrameworkData(
  framework: DataTypeEnum,
  dataId: string,
  apiClientProvider: ReturnType<typeof useApiClient>
): Promise<CompanyAssociatedData<object>> {
  const api = getFrameworkDataApiForIdentifier(framework, apiClientProvider);
  if (!api) {
    throw new Error(`No data API for framework: ${framework}`);
  }
  const response = await api.getFrameworkData(dataId);
  return response.data;
}

/**
 * Vue Query hook to fetch framework-specific company-associated data.
 *
 * @param {{ framework: MaybeRef<DataTypeEnum|undefined>, dataId: MaybeRef<string|undefined> }} params -
 *   Reactive or static values for framework and data id.
 * @returns {UseQueryReturnType<CompanyAssociatedData<object>, Error>} Query result for the requested data.
 */
export function useGetFrameworkDataQuery(params: {
  framework: MaybeRef<DataTypeEnum | undefined>;
  dataId: MaybeRef<string | undefined>;
}): UseQueryReturnType<CompanyAssociatedData<object>, Error> {
  const apiClientProvider = useApiClient();

  const framework = computed(() => unref(params.framework));
  const dataId = computed(() => unref(params.dataId));

  const queryKey = computed(() => frameworkDataKeys.byFrameworkAndId(framework.value, dataId.value));
  const enabled = computed(() => !!framework.value && !!dataId.value);

  return useQuery({
    queryKey,
    enabled,
    queryFn: async () => {
      if (!framework.value || !dataId.value) {
        throw new Error('Framework and dataId must be defined');
      }
      return fetchFrameworkData(framework.value, dataId.value, apiClientProvider);
    },
  });
}
