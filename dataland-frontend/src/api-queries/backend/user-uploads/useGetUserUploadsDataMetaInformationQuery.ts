import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useQuery, type UseQueryOptions, type UseQueryReturnType } from '@tanstack/vue-query';
import { useApiClient } from '@/utils/useApiClient';
import { userUploadsKeys } from '@/api-queries/backend/user-uploads/userUploadsKeys';
import { type DataMetaInformationForMyDatasets } from '@clients/backend';

/**
 * Vue Query hook that fetches the meta information of all datasets uploaded by a user (GET /users/{userId}/uploads).
 * The query is disabled while `userId` is empty.
 *
 * @param params - Endpoint parameters.
 * @param params.userId - Id of the user whose uploads are listed.
 * @param options - Additional TanStack query options. A passed `enabled` replaces the default.
 * @returns Vue Query result containing the {@link DataMetaInformationForMyDatasets} of the uploads.
 */
export function useGetUserUploadsDataMetaInformationQuery(
  params: { userId: MaybeRefOrGetter<string | undefined> },
  options: Omit<UseQueryOptions<DataMetaInformationForMyDatasets[], Error>, 'queryKey' | 'queryFn'> = {}
): UseQueryReturnType<DataMetaInformationForMyDatasets[], Error> {
  const apiClientProvider = useApiClient();

  return useQuery<DataMetaInformationForMyDatasets[], Error>({
    queryKey: computed(() => userUploadsKeys.dataMetaInformation(toValue(params.userId))),
    queryFn: async () => {
      const userId = toValue(params.userId);
      if (!userId) throw new Error('userId is undefined');
      const response =
        await apiClientProvider.backendClients.userUploadsController.getUserUploadsDataMetaInformation(userId);
      return response.data;
    },
    enabled: computed(() => !!toValue(params.userId)),
    ...options,
  });
}
