import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import {PreApprovalConfig} from "@clients/qaservice";
import { useApiClient } from '@/utils/useApiClient.ts';
import { preApprovalConfigKeys } from '@/api-queries/qa-service/pre-approval/preApprovalConfigKeys.ts';

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
