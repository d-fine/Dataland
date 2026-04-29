import { type Ref, computed } from 'vue';
import { useQuery, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient.ts';
import { requestKeys } from '@/api-queries/community/request/requestKeys.ts';
import type {
  AccessStatus,
  ExtendedStoredDataRequest,
  GetDataRequestsDataTypeEnum,
  RequestPriority,
  RequestStatus,
} from '@clients/communitymanager';

export type GetStoredDataRequestsQueryParams = {
  dataType?: GetDataRequestsDataTypeEnum[];
  userId?: string;
  emailAddress?: string;
  adminComment?: string;
  requestStatus?: RequestStatus[];
  accessStatus?: AccessStatus[];
  requestPriority?: RequestPriority[];
  reportingPeriods?: string[];
  datalandCompanyId?: string;
  companySearchString?: string;
  chunkSize?: number;
  chunkIndex?: number;
};

const toSet = <T>(values?: T[]): Set<T> | undefined => (values ? new Set(values) : undefined);

/**
 * Vue Query hook that fetches stored data requests based on optional filters.
 *
 * @param params - Reactive reference containing optional request filters.
 * @returns Query result containing matching stored data requests.
 */
export function useGetStoredDataRequestsQuery(
  params: Ref<GetStoredDataRequestsQueryParams>
): UseQueryReturnType<ExtendedStoredDataRequest[], Error> {
  const apiClientProvider = useApiClient();

  const queryKey = computed(() => requestKeys.filtered(params.value));

  return useQuery<ExtendedStoredDataRequest[], Error>({
    queryKey,
    queryFn: async () => {
      const parameters = params.value;

      const response = await apiClientProvider.apiClients.communityManagerRequestController.getDataRequests(
        toSet(parameters.dataType),
        parameters.userId,
        parameters.emailAddress,
        parameters.adminComment,
        toSet(parameters.requestStatus),
        toSet(parameters.accessStatus),
        toSet(parameters.requestPriority),
        toSet(parameters.reportingPeriods),
        parameters.datalandCompanyId,
        parameters.companySearchString,
        parameters.chunkSize,
        parameters.chunkIndex
      );

      return response.data;
    },
  });
}
