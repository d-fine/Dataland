import { useMutation, useQueryClient, type UseMutationReturnType } from '@tanstack/vue-query';
import { AcceptedDataPointSource, type DataPointJudgement } from '@clients/qaservice';
import { useApiClient } from '@/utils/useApiClient.ts';
import { datasetJudgementKeys } from '@/api-queries/qa-service/dataset-judgement/datasetJudgementKeys.ts';

export interface AcceptAllRemainingOriginalDataPointsArgs {
  judgementId: string;
  dataPoints: Record<string, DataPointJudgement>;
}

/**
 * Maximum number of PATCH requests to have in flight at the same time when accepting
 * all remaining original data points, to avoid overwhelming the backend with a large
 * burst of parallel requests for datasets with many data points (e.g. EU Taxonomy).
 */
const MAX_CONCURRENT_REQUESTS = 10;

/**
 * Splits an array into consecutive chunks of at most the given size.
 * @param items - The array to split into chunks.
 * @param chunkSize - The maximum size of each chunk.
 * @returns An array of chunks.
 */
function chunk<T>(items: T[], chunkSize: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < items.length; i += chunkSize) {
    chunks.push(items.slice(i, i + chunkSize));
  }
  return chunks;
}

/**
 * Vue Query mutation hook that accepts the original data point value for every data point
 * of a dataset judgement that has not yet been reviewed (i.e. `acceptedSource === undefined`).
 *
 * Already reviewed data points (with an accepted source of Original, Qa or Custom) are left
 * untouched. Each affected data point is patched individually using the same request the
 * "ACCEPT ORIGINAL" action in the Judge dialog uses, so the resulting state is identical to
 * what a user would get by accepting each data point one by one.
 *
 * Failures for individual data points are logged but do not prevent the remaining requests
 * from being sent; failed data points simply stay unreviewed.
 */
export function useAcceptAllRemainingOriginalDataPointsMutation(): UseMutationReturnType<
  void,
  Error,
  AcceptAllRemainingOriginalDataPointsArgs,
  unknown
> {
  const apiClientProvider = useApiClient();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ judgementId, dataPoints }: AcceptAllRemainingOriginalDataPointsArgs) => {
      const remainingDataPointTypeIds = Object.values(dataPoints)
        .filter((dataPoint) => dataPoint.acceptedSource == null)
        .map((dataPoint) => dataPoint.dataPointType);

      for (const batch of chunk(remainingDataPointTypeIds, MAX_CONCURRENT_REQUESTS)) {
        await Promise.allSettled(
          batch.map((dataPointTypeId) =>
            apiClientProvider.apiClients.datasetJudgementController
              .patchJudgementDetails(judgementId, dataPointTypeId, {
                acceptedSource: AcceptedDataPointSource.Original,
              })
              .catch((error: unknown) => {
                console.error(
                  `Error accepting original data point for dataPointType: ${dataPointTypeId} ` +
                    `while accepting all remaining original data points.`,
                  error
                );
              })
          )
        );
      }
    },

    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: datasetJudgementKeys.detail(variables.judgementId),
      });
    },

    onError: (error) => {
      console.error('Error accepting all remaining original data points:', error);
    },
  });
}
