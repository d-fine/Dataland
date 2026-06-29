import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { PreApprovalConfig } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient';

/**
 * Provides a query for fetching the pre-approval configuration.
 *
 * @returns A Vue Query result containing the {@link PreApprovalConfig} data
 *          or an {@link Error} if the request fails.
 */
export function usePreApprovalConfigQuery(): UseQueryReturnType<PreApprovalConfig, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<PreApprovalConfig>({
    queryKey: ['preApprovalConfig'],
    queryFn: async () => {
      const { data } = await apiClientProvider.apiClients.preApprovalController.getPreApprovalConfig();
      return data;
    },
  });
}
