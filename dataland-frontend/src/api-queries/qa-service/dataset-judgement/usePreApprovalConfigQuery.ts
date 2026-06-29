import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import type { PreApprovalConfig } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient';

export function usePreApprovalConfigQuery():
  UseQueryReturnType<PreApprovalConfig, Error> {
  const apiClientProvider = useApiClient();

  return useQuery<PreApprovalConfig>({
    queryKey: ['preApprovalConfig'],
    queryFn: async () => {
      const { data } = await apiClientProvider.apiClients.
      preApprovalController.getPreApprovalConfig();
      return data;
    },
  });
}