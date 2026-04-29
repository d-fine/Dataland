import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { requestKeys } from '@/api-queries/data-sourcing/request/requestKeys.ts';
import type { StoredRequest } from '@clients/datasourcingservice';

/**
 * Vue Query hook that fetches stored requests for a given request id.
 *
 * @param {Ref<string|undefined>} requestId - Reactive reference containing the request id; query runs when truthy.
 * @returns {UseQueryReturnType<StoredRequest, Error>} Query result containing the stored request.
 */
export function useGetRequestByDataRequestIdQuery(
  requestId: Ref<string | undefined>
): UseQueryReturnType<StoredRequest, Error> {
  const apiClientProvider = useApiClient();
  const queryKey = computed(() => requestKeys.byRequestId(requestId.value));

  return useQuery<StoredRequest, Error>({
    queryKey,
    queryFn: async () => {
      if (!requestId.value) {
        throw new Error('requestId is undefined');
      }
      const response = await apiClientProvider.apiClients.requestController.getRequest(requestId.value);
      return response.data;
    },
  });
}
