import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { PreApprovalConfig } from 'build/clients/qaservice';
import { useApiClient } from 'src/utils/useApiClient';
import { preApprovalConfigKeys } from "./preApprovalConfigKeys.ts";

/**
 * Provides a query for fetching the pre-approval configuration.
 *
 * @returns A Vue Query result containing the {@link PreApprovalConfig} data
 *          or an {@link Error} if the request fails.
 */
export function usePreApprovalConfigQuery(): UseQueryReturnType<PreApprovalConfig, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<PreApprovalConfig>({
    queryKey: preApprovalConfigKeys.all,
    queryFn: async () => {
      const { data } = await apiClientProvider.apiClients.preApprovalController.getPreApprovalConfig();
      return data;
    },
  });
}
